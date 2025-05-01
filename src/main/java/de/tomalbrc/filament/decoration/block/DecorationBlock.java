package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.VirtualDestroyStage;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.BiConsumer;

public abstract class DecorationBlock extends Block implements PolymerBlock, BlockWithElementHolder, SimpleWaterloggedBlock, VirtualDestroyStage.Marker {
    final protected ResourceLocation decorationId;

    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected final StateDefinition<Block, BlockState> stateDefinitionEx;

    public DecorationBlock(Properties properties, ResourceLocation decorationId) {
        super(properties);
        this.decorationId = decorationId;

        // the StateDefinition built too early, cant access DecorationData from within createBlockStateDefinition
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinitionEx = builder.create(Block::defaultBlockState, BlockState::new);

        var data = getDecorationData();
        var state = this.stateDefinitionEx.any();
        if (data.isLightEnabled()) {
            state = state.setValue(LIGHT_LEVEL, !data.hasLightBehaviours() && data.properties().mayBeLightSource() && !data.properties().lightEmission.isMap() ? data.properties().lightEmission.getRawValue() : 0);
        }
        if (data.properties().waterloggable) {
            state = state.setValue(WATERLOGGED, false);
        }
        this.registerDefaultState(state);

        this.stateDefinitionEx.getPossibleStates().forEach(BlockState::initCache);
        this.stateDefinition.getPossibleStates().forEach(BlockState::initCache);
    }

    @Override
    @NotNull
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinitionEx;
    }

    public DecorationData getDecorationData() {
        return DecorationRegistry.getDecorationDefinition(this.decorationId);
    }

    @Override
    public boolean forceLightUpdates(BlockState blockState) { return blockState.hasProperty(LIGHT_LEVEL) && blockState.getValue(LIGHT_LEVEL) > 0; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (this.decorationId == null)
            return;

        DecorationData data = getDecorationData();
        if (data != null) {
            if (data.isLightEnabled()) {
                builder.add(LIGHT_LEVEL);
            }

            if (data.properties().waterloggable) {
                builder.add(WATERLOGGED);
            }
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
        DecorationData decorationData = getDecorationData();
        boolean passthrough = !decorationData.hasBlocks();
        BlockState defaultState = passthrough ? state.hasProperty(DecorationBlock.WATERLOGGED) && state.getValue(DecorationBlock.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState() : decorationData.block();

        if (state.hasProperty(DecorationBlock.WATERLOGGED) && defaultState.hasProperty(DecorationBlock.WATERLOGGED) && state.getValue(DecorationBlock.WATERLOGGED) && !passthrough) {
            defaultState = defaultState.setValue(DecorationBlock.WATERLOGGED, true);
        }

        return defaultState;
    }

    @Override
    public void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (!blockState.isAir()) {
            this.removeDecoration(level, blockPos, null);
        }
    }

    @Override
    @NotNull
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockState returnVal = super.playerWillDestroy(level, blockPos, blockState, player);
        this.removeDecoration(level, blockPos, player);
        return returnVal;
    }

    private void removeDecoration(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            decorationBlockEntity.destroyStructure(player == null || !player.isCreative());
        } else {
            level.destroyBlock(blockPos, false);
        }
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!getDecorationData().hasBlocks()) {
            return Shapes.empty();
        } else {
            return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
        }
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        if (DecorationRegistry.isDecoration(blockState)) {
            DecorationData data = ((DecorationBlock)blockState.getBlock()).getDecorationData();
            if (data != null && (data.properties().waterloggable || !data.properties().solid)) {
                return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
            }
        }

        return false;
    }

    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (DecorationRegistry.isDecoration(blockState) && ((DecorationBlock)blockState.getBlock()).getDecorationData() != null) {
            DecorationData data = ((DecorationBlock)blockState.getBlock()).getDecorationData();
            if (data.properties().waterloggable) {
                return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    @NotNull
    public FluidState getFluidState(BlockState blockState) {
        return blockState.hasProperty(WATERLOGGED) && blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    @NotNull
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.hasProperty(WATERLOGGED) && blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.isSource() && fluidState.getType() == Fluids.WATER;
        var res = super.getStateForPlacement(blockPlaceContext);
        assert res != null;
        return res.hasProperty(WATERLOGGED) ? res.setValue(WATERLOGGED, bl) : res;
    }
}
