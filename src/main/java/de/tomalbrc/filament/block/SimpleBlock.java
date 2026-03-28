package de.tomalbrc.filament.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.FireBlockHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleBlock extends Block implements PolymerTexturedBlock, BehaviourHolder, SimpleWaterloggedBlock, BonemealableBlock, WeatheringCopper, Fallable, FireBlockHooks {
    protected Map<BlockState, BlockData.BlockStateMeta> stateMap;
    protected final BlockState breakEventState;
    protected final AbstractBlockData<? extends BlockProperties> blockData;

    protected final StateDefinition<Block, BlockState> stateDefinitionEx;

    private final BehaviourMap behaviours = new BehaviourMap();

    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    public SimpleBlock(BlockBehaviour.Properties properties, AbstractBlockData<? extends BlockProperties> data) {
        super(properties);

        this.initBehaviours(data.behaviour());
        this.breakEventState = data.properties().blockBase().defaultBlockState();
        this.blockData = data;

        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : behaviours) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                blockBehaviour.modifyBlockProperties(properties);
            }
        }

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

    public AbstractBlockData<? extends BlockProperties> data() {
        return this.blockData;
    }

    public SimpleBlock asFilamentBlock() {
        return this;
    }

    @Override
    public @NotNull MutableComponent getName() {
        var dataName = this.blockData.displayName();
        return dataName != null ? dataName.copy() : super.getName();
    }

    @Override
    public boolean forceLightUpdates(BlockState blockState) {
        return true;
    }

    @Override
    @NotNull
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinitionEx;
    }

    public void postRegister() {
        this.stateMap = this.blockData.createStandardStateMap();
        if (this.stateMap != null) this.forEach(behaviour -> behaviour.modifyStateMap(this.stateMap, this.blockData));
        this.stateDefinitionEx.getPossibleStates().forEach(BlockState::initCache);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        BlockState state = blockState;
        if (this.stateMap != null) {
            state = behaviourModifiedBlockState(blockState, state);
        }

        return this.stateMap != null && this.stateMap.get(state) != null ? this.stateMap.get(state).blockState() : Blocks.BEDROCK.defaultBlockState();
    }

    public BlockState behaviourModifiedBlockState(BlockState original, BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                original = blockBehaviour.modifyPolymerBlockState(original, blockState);
            }
        }
        return original;
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
    public BlockState playerWillDestroy(@NonNull Level level, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull Player player) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.playerWillDestroy(level, blockPos, blockState, player));

        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos blockPos, @NonNull BlockState blockState, LivingEntity livingEntity, @NonNull ItemStack itemStack) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack));
    }

    @Override
    public void affectNeighborsAfterRemoval(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, boolean bl) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.affectNeighborsAfterRemoval(blockState, serverLevel, blockPos, bl));
    }

    @Override
    protected long getSeed(@NonNull BlockState blockState, @NonNull BlockPos blockPos) {
        if (this.getBehaviours() != null)
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    var res = blockBehaviour.getSeed(blockState, blockPos);
                    if (res != null && res.isPresent())
                        return res.orElseThrow();
                }
            }

        return super.getSeed(blockState, blockPos);
    }

    @Override
    public void onLand(@NonNull Level level, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull BlockState blockState2, @NonNull FallingBlockEntity fallingBlockEntity) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onLand(level, blockPos, blockState, blockState2, fallingBlockEntity));
    }

    @Override
    public void onBrokenAfterFall(@NonNull Level level, @NonNull BlockPos blockPos, @NonNull FallingBlockEntity fallingBlockEntity) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onBrokenAfterFall(level, blockPos, fallingBlockEntity));
    }

    @Override
    @NotNull
    public DamageSource getFallDamageSource(@NonNull Entity entity) {
        if (this.getBehaviours() != null)
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    var res = blockBehaviour.getFallDamageSource(entity);
                    if (res != null)
                        return res;
                }
            }

        return Fallable.super.getFallDamageSource(entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.createBlockStateDefinition(builder));
    }

    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext blockPlaceContext) {
        BlockState def = super.getStateForPlacement(blockPlaceContext);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                def = blockBehaviour.getStateForPlacement(def, blockPlaceContext);
            }
        }

        return def;
    }

    @Override
    public void neighborChanged(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Block block, Orientation orientation, boolean bl) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.neighborChanged(blockState, level, blockPos, block, orientation, bl));
        super.neighborChanged(blockState, level, blockPos, block, orientation, bl);
    }

    @Override
    public void onExplosionHit(@NonNull BlockState blockState, @NonNull ServerLevel level, @NonNull BlockPos blockPos, Explosion explosion, @NonNull BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (explosion.getDirectSourceEntity() instanceof Player player && !CommonProtection.canExplodeBlock(level, blockPos, explosion, player.nameAndId(), player))
            return;

        if (this.getBehaviours() != null)
            this.forEach(x -> x.onExplosionHit(blockState, level, blockPos, explosion, biConsumer));

        if (!blockState.isAir() && (explosion.getBlockInteraction() == Explosion.BlockInteraction.DESTROY || explosion.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY)) {
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
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void wasExploded(@NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull Explosion explosion) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.wasExploded(serverLevel, blockPos, explosion));
    }

    @Override
    public boolean dropFromExplosion(@NonNull Explosion explosion) {
        if (this.getBehaviours() != null)
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
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
    public BlockState rotate(@NonNull BlockState blockState, @NonNull Rotation rotation) {
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
    public BlockState mirror(@NonNull BlockState blockState, @NonNull Mirror mirror) {
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
    public boolean isSignalSource(@NonNull BlockState blockState) {
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
    public int getDirectSignal(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull Direction direction) {
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
    public int getSignal(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull Direction direction) {
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
    protected boolean useShapeForLightOcclusion(@NonNull BlockState blockState) {
        if (this.getBehaviours() != null)
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    var res = blockBehaviour.useShapeForLightOcclusion(blockState);
                    if (res.isPresent())
                        return res.get();
                }
            }
        return super.useShapeForLightOcclusion(blockState);
    }

    @Override
    protected boolean canBeReplaced(@NonNull BlockState blockState, @NonNull BlockPlaceContext blockPlaceContext) {
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
    protected boolean canBeReplaced(BlockState blockState, @NonNull Fluid fluid) {
        return blockState.canBeReplaced() || !blockData.properties().solid();
    }

    @Override
    @NotNull
    protected FluidState getFluidState(@NonNull BlockState blockState) {
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
    public boolean placeLiquid(@NonNull LevelAccessor levelAccessor, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull FluidState fluidState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof SimpleWaterloggedBlock waterloggedBlock) {
                var res = waterloggedBlock.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
                if (res) {
                    return true;
                }
            }
        }

        if (!blockData.properties().solid()) {
            levelAccessor.destroyBlock(blockPos, true);
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), Block.UPDATE_ALL);
        }

        return !blockData.properties().solid();
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity livingEntity, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull Fluid fluid) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof SimpleWaterloggedBlock waterloggedBlock) {
                return waterloggedBlock.canPlaceLiquid(livingEntity, blockGetter, blockPos, blockState, fluid);
            }
        }
        return !blockData.properties().solid();
    }

    @Override
    @NotNull
    protected BlockState updateShape(@NonNull BlockState blockState, @NonNull LevelReader levelReader, @NonNull ScheduledTickAccess scheduledTickAccess, @NonNull BlockPos blockPos, @NonNull Direction direction, @NonNull BlockPos blockPos2, @NonNull BlockState blockState2, @NonNull RandomSource randomSource) {
        var bs = super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                bs = blockBehaviour.updateShape(bs, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
            }
        }
        return bs;
    }

    @Override
    protected boolean isPathfindable(@NonNull BlockState blockState, @NonNull PathComputationType pathComputationType) {
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
    protected void onPlace(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        this.forEach(x -> x.onPlace(blockState, level, blockPos, blockState2, bl));
    }

    @Override
    protected void spawnAfterBreak(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull ItemStack itemStack, boolean bl) {
        this.forEach(x -> x.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl));
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
    }

    @Override
    @NotNull
    public ItemStack getCloneItemStack(@NonNull LevelReader levelReader, @NonNull BlockPos blockPos, @NonNull BlockState blockState, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(levelReader, blockPos, blockState, includeData);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var item = blockBehaviour.getCloneItemStack(stack, levelReader, blockPos, blockState, includeData);
                if (item != null) {
                    stack = item;
                }
            }
        }

        return stack;
    }

    @Override
    public boolean canSurvive(@NonNull BlockState blockState, @NonNull LevelReader levelReader, @NonNull BlockPos blockPos) {
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
    public void tick(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull RandomSource randomSource) {
        super.tick(blockState, serverLevel, blockPos, randomSource);
        this.forEach(x -> x.tick(blockState, serverLevel, blockPos, randomSource));
    }

    // random ticking

    @Override
    protected boolean isRandomlyTicking(@NonNull BlockState blockState) {
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
    protected void randomTick(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull RandomSource randomSource) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.randomTick(blockState, serverLevel, blockPos, randomSource));
    }

    // bonemealable impl

    @Override
    public boolean isValidBonemealTarget(@NonNull LevelReader levelReader, @NonNull BlockPos blockPos, @NonNull BlockState blockState) {
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
    public boolean isBonemealSuccess(@NonNull Level level, @NonNull RandomSource randomSource, @NonNull BlockPos blockPos, @NonNull BlockState blockState) {
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
    public void performBonemeal(@NonNull ServerLevel serverLevel, @NonNull RandomSource randomSource, @NonNull BlockPos blockPos, @NonNull BlockState blockState) {
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
    public InteractionResult useWithoutItem(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Player player, @NonNull BlockHitResult blockHitResult) {
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
    public InteractionResult useItemOn(@NonNull ItemStack itemStack, @NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Player player, @NonNull InteractionHand interactionHand, @NonNull BlockHitResult blockHitResult) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
                if (res != null && res.consumesAction())
                    return res;
            }
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected void attack(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Player player) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.attack(blockState, level, blockPos, player));
    }

    @Override
    public void onProjectileHit(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockHitResult blockHitResult, @NonNull Projectile projectile) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.onProjectileHit(level, blockState, blockHitResult, projectile));
    }

    // --- oxidization

    @Override
    public void changeOverTime(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull RandomSource randomSource) {
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
    public Optional<BlockState> getNextState(@NonNull BlockState blockState, @NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, @NonNull RandomSource randomSource) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> && behaviour.getValue() instanceof WeatheringCopper weatheringCopper) {
                return weatheringCopper.getNextState(blockState, serverLevel, blockPos, randomSource);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean hasAnalogOutputSignal(@NonNull BlockState blockState) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.hasAnalogOutputSignal(blockState);
                if (res.isPresent())
                    return res.orElseThrow();
            }
        }

        return super.hasAnalogOutputSignal(blockState);
    }

    @Override
    public int getAnalogOutputSignal(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Direction direction) {
        int max = super.getAnalogOutputSignal(blockState, level, blockPos, direction);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getAnalogOutputSignal(blockState, level, blockPos, direction);
                if (res > max)
                    max = res;
            }
        }

        return max;
    }

    @Override
    public void entityInside(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Entity entity, @NonNull InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (this.getBehaviours() != null) {
            this.getBehaviours().forEach(behaviourTypeBehaviourEntry -> {
                if (behaviourTypeBehaviourEntry.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviourWithEntity)
                    blockBehaviourWithEntity.entityInside(blockState, level, blockPos, entity, insideBlockEffectApplier);
            });
        }
    }

    @Override
    public FlammableBlockRegistry.@NonNull Entry fabric_getVanillaEntry(@NonNull BlockState blockState) {
        return new FlammableBlockRegistry.Entry(0, 0);
    }

    @Override
    protected int getLightDampening(@NonNull BlockState state) {
        if (this.getBehaviours() != null) {
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    var res = blockBehaviour.getLightDampening(state);
                    if (res != -1)
                        return res;
                }
            }
        }

        return super.getLightDampening(state);
    }

    @Override
    @NotNull
    protected VoxelShape getBlockSupportShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        if (this.getBehaviours() != null) {
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    var res = blockBehaviour.getBlockSupportShape(state, level, pos);
                    if (res != null)
                        return res;
                }
            }
        }

        return super.getBlockSupportShape(state, level, pos);
    }

    @Override
    public void updateEntityMovementAfterFallOn(@NonNull BlockGetter blockGetter, @NonNull Entity entity) {
        boolean ranCustomImpl = false;
        if (this.getBehaviours() != null) {
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    ranCustomImpl = ranCustomImpl || blockBehaviour.updateEntityMovementAfterFallOn(blockGetter, entity);
                }
            }
        }

        if (!ranCustomImpl) {
            super.updateEntityMovementAfterFallOn(blockGetter, entity);
        }
    }

    @Override
    public void fallOn(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockPos blockPos, @NonNull Entity entity, double d) {
        boolean ranCustomImpl = false;
        if (this.getBehaviours() != null) {
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                    ranCustomImpl |= blockBehaviour.fallOn(level, blockState, blockPos, entity, d);
                }
            }
        }

        if (!ranCustomImpl) {
            super.fallOn(level, blockState, blockPos, entity, d);
        }
    }

    @Override
    public void stepOn(@NonNull Level level, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull Entity entity) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.stepOn(level, blockPos, blockState, entity));
    }

    @Override
    protected void spawnDestroyParticles(@NonNull Level level, @NonNull Player player, @NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        if (blockData.properties().showBreakParticles()) {
            super.spawnDestroyParticles(level, player, blockPos, blockState);
        }
    }
}
