package de.tomalbrc.filament.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleBlock extends Block implements PolymerTexturedBlock, BehaviourHolder, SimpleWaterloggedBlock, BonemealableBlock, WeatheringCopper, Fallable {
    protected Map<BlockState, BlockData.BlockStateMeta> stateMap;
    protected final BlockState breakEventState;
    protected final BlockData blockData;

    protected final StateDefinition<Block, BlockState> stateDefinitionEx;

    private final BehaviourMap behaviours = new BehaviourMap();

    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    public SimpleBlock(BlockBehaviour.Properties properties, BlockData data) {
        super(properties);

        this.initBehaviours(data.behaviourConfig());
        this.breakEventState = data.properties().blockBase.defaultBlockState();
        this.blockData = data;

        // the StateDefinition built too early, cant access BlockData from within createBlockStateDefinition
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinitionEx = builder.create(Block::defaultBlockState, BlockState::new);

        BlockState[] def = {this.stateDefinitionEx.any()};
        this.forEach(behaviour -> def[0] = behaviour.modifyDefaultState(def[0]));
        this.registerDefaultState(def[0]);
    }

    public boolean hasData() {
        return this.blockData != null;
    }

    @Override
    @NotNull
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinitionEx;
    }

    public void postRegister() {
        this.stateMap = this.blockData.createStandardStateMap();
        this.forEach(behaviour -> behaviour.modifyStateMap(this.stateMap, this.blockData));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                BlockState polyBlockState = blockBehaviour.getCustomPolymerBlockState(this.stateMap, blockState);
                if (polyBlockState != null)
                    return polyBlockState;
            }
        }
        return this.stateMap.get(blockState) != null ? this.stateMap.get(blockState).blockState() : Blocks.BEDROCK.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    private void forEach(Consumer<de.tomalbrc.filament.api.behaviour.BlockBehaviour<?>> consumer) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                consumer.accept(blockBehaviour);
            }
        }
    }

    /// --- behaviour impl

    @Override
    @NotNull
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.playerWillDestroy(level, blockPos, blockState, player));
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }


    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack));
    }

    @Override
    protected long getSeed(BlockState blockState, BlockPos blockPos) {
        if (this.getBehaviours() != null) for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getSeed(blockState, blockPos);
                if (res != null && res.isPresent())
                    return res.orElseThrow();
            }
        }

        return Mth.getSeed(blockPos);
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onLand(level, blockPos, blockState, blockState2, fallingBlockEntity));
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onBrokenAfterFall(level, blockPos, fallingBlockEntity));
    }

    @Override
    @NotNull
    public DamageSource getFallDamageSource(Entity entity) {
        if (this.getBehaviours() != null) for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getFallDamageSource(entity);
                if (res != null)
                    return res;
            }
        }

        return Fallable.super.getFallDamageSource(entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.createBlockStateDefinition(builder));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState def = this.defaultBlockState();
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                def = blockBehaviour.getStateForPlacement(def, blockPlaceContext);
            }
        }

        return def;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.neighborChanged(blockState, level, blockPos, block, blockPos2, bl));
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    @Override
    public void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onExplosionHit(blockState, level, blockPos, explosion, biConsumer));

        if (!blockState.isAir() && explosion.getBlockInteraction() != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            Block block = blockState.getBlock();
            boolean bl = explosion.getIndirectSourceEntity() instanceof Player;
            if (block.dropFromExplosion(explosion)) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                LootParams.Builder builder = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getDirectSourceEntity());
                if (explosion.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                    builder.withParameter(LootContextParams.EXPLOSION_RADIUS, explosion.radius());
                }

                blockState.spawnAfterBreak(level, blockPos, ItemStack.EMPTY, bl);
                blockState.getDrops(builder).forEach((itemStack) -> biConsumer.accept(itemStack, blockPos));
            }

            this.wasExploded(level, blockPos, explosion); // switch up order to support mapped blockstate properties in block behaviours (tnt example)
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void wasExploded(Level serverLevel, BlockPos blockPos, Explosion explosion) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.wasExploded((ServerLevel)serverLevel, blockPos, explosion));
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        if (this.getBehaviours() != null) for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.dropFromExplosion(explosion);
                if (!res)
                    return false;
            }
        }

        return super.dropFromExplosion(explosion);
    }

    @Override
    @NotNull
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.rotate(blockState, rotation);
                if (res != null)
                    return res;
            }
        }
        return super.rotate(blockState, rotation);
    }

    @Override
    @NotNull
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.mirror(blockState, mirror);
                if (res != null)
                    return res;
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                blockBehaviour.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
            }
        }
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.isSignalSource(blockState);
                if (res)
                    return true;
            }
        }
        return super.isSignalSource(blockState);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getDirectSignal(blockState, blockGetter, blockPos, direction);
                if (res > 0)
                    return res;
            }
        }
        return super.getDirectSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getSignal(blockState, blockGetter, blockPos, direction);
                if (res > 0)
                    return res;
            }
        }
        return super.getSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        if (this.getBehaviours() != null) for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.useShapeForLightOcclusion(blockState);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.useShapeForLightOcclusion(blockState);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.canBeReplaced(blockState, blockPlaceContext);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return blockState.canBeReplaced() || !blockData.properties().solid;
    }

    @Override
    @NotNull
    protected FluidState getFluidState(BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getFluidState(blockState);
                if (res != null)
                    return res;
            }
        }
        return super.getFluidState(blockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour && blockBehaviour instanceof SimpleWaterloggedBlock waterloggedBlock) {
                var res = waterloggedBlock.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
                if (res) {
                    return true;
                }
            }
        }

        if (!blockData.properties().solid) {
            levelAccessor.destroyBlock(blockPos, true);
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 0);
        }

        return !blockData.properties().solid;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour && blockBehaviour instanceof SimpleWaterloggedBlock waterloggedBlock) {
                return waterloggedBlock.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid);
            }
        }
        return !blockData.properties().solid;
    }

    @Override
    @NotNull
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        var bs = super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                bs = blockBehaviour.updateShape(bs, direction, blockState2, levelAccessor, blockPos, blockPos2);
            }
        }
        return bs;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.isPathfindable(blockState, pathComputationType);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.isPathfindable(blockState, pathComputationType);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        this.forEach(x -> x.onPlace(blockState, level, blockPos, blockState2, bl));
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.forEach(x -> x.onRemove(blockState, level, blockPos, blockState2, bl));
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    @NotNull
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        ItemStack stack = super.getCloneItemStack(levelReader, blockPos, blockState);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var item = blockBehaviour.getCloneItemStack(stack, levelReader, blockPos, blockState);
                if (item != null) {
                    stack = item;
                }
            }
        }

        return stack;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.canSurvive(blockState, levelReader, blockPos);
                if (!res)
                    return false;
            }
        }
        return true;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        super.tick(blockState, serverLevel, blockPos, randomSource);
        this.forEach(x -> x.tick(blockState, serverLevel, blockPos, randomSource));
    }


    // random ticking

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.isRandomlyTicking(blockState);
                if (res)
                    return true;
            }
        }
        return super.isRandomlyTicking(blockState);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.randomTick(blockState, serverLevel, blockPos, randomSource));
    }

    // bonemealable impl

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof BonemealableBlock bonemealableBlock) {
                var res = bonemealableBlock.isValidBonemealTarget(levelReader, blockPos, blockState);
                if (res)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof BonemealableBlock bonemealableBlock) {
                var res = bonemealableBlock.isBonemealSuccess(level, randomSource, blockPos, blockState);
                if (res)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof BonemealableBlock bonemealableBlock) {
                bonemealableBlock.performBonemeal(serverLevel, randomSource, blockPos, blockState);
            }
        }
    }

    @Override
    @NotNull
    public Type getType() {
        var def = BonemealableBlock.super.getType();
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof BonemealableBlock bonemealableBlock) {
                var res = bonemealableBlock.getType();
                if (!res.equals(def))
                    return res;
            }
        }
        return def;
    }

    // -- interaction

    @Override
    @NotNull
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
                if (res != null && res.consumesAction())
                    return res;
            }
        }
        return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }

    @Override
    @NotNull
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
                if (res != null && res.consumesAction())
                    return res;
            }
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onProjectileHit(level, blockState, blockHitResult, projectile));
    }

    // --- oxidization

    @Override
    public void changeOverTime(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof WeatheringCopper weatheringCopper) {
                weatheringCopper.changeOverTime(blockState, serverLevel, blockPos, randomSource);
            }
        }
    }

    @Override
    @NotNull
    public WeatherState getAge() {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof WeatheringCopper weatheringCopper) {
                return weatheringCopper.getAge();
            }
        }
        return WeatherState.OXIDIZED;
    }

    @Override
    @NotNull
    public Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof WeatheringCopper weatheringCopper) {
                return weatheringCopper.getNextState(blockState, serverLevel, blockPos, randomSource);
            }
        }
        return Optional.empty();
    }
}
