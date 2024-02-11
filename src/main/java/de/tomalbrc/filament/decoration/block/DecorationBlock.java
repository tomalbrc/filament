package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public abstract class DecorationBlock extends Block implements PolymerBlock, SimpleWaterloggedBlock {
    final protected ResourceLocation decorationId;

    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty PASSTHROUGH = BooleanProperty.create("passthrough");

    public DecorationBlock(Properties properties, ResourceLocation decorationId) {
        super(properties);
        this.decorationId = decorationId;

        // todo
        //FlammableBlockRegistry.getDefaultInstance().add(this, 5, 10);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIGHT_LEVEL, 0)
                .setValue(PASSTHROUGH, false)
                .setValue(WATERLOGGED, false)
        );
    }

    public DecorationData getDecorationData() {
        return DecorationRegistry.getDecorationDefinition(this.decorationId);
    }

    @Override
    public boolean forceLightUpdates(BlockState blockState) { return blockState.getValue(LIGHT_LEVEL) > 0; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, PASSTHROUGH, WATERLOGGED);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return state.getValue(DecorationBlock.PASSTHROUGH) ? state.getValue(DecorationBlock.WATERLOGGED) ? Blocks.WATER : Blocks.AIR : Blocks.BARRIER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return state.getValue(DecorationBlock.WATERLOGGED) && !state.getValue(DecorationBlock.PASSTHROUGH) ?
                this.getPolymerBlock(state).defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true) :
                this.getPolymerBlock(state).defaultBlockState();
    }

    @Override
    public void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (!blockState.isAir() && explosion.getBlockInteraction() != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            this.removeDecoration(level, blockPos, null);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockState returnVal = super.playerWillDestroy(level, blockPos, blockState, player);
        this.removeDecoration(level, blockPos, player);
        return returnVal;
    }

    private void removeDecoration(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            decorationBlockEntity.destroyStructure(player == null || !player.isCreative());
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(PASSTHROUGH)) {
            return Shapes.empty();
        } else {
            return Shapes.block();
        }
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        if (blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity &&
            decorationBlockEntity.getDecorationData() != null &&
            decorationBlockEntity.getDecorationData().properties() != null &&
            decorationBlockEntity.getDecorationData().properties().waterloggable)
        {
            return SimpleWaterloggedBlock.super.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid);
        }
        return false;
    }

    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (levelAccessor.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity && decorationBlockEntity.getDecorationData() != null && decorationBlockEntity.getDecorationData().properties() != null) {
            if (decorationBlockEntity.getDecorationData().properties().waterloggable) {
                return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
            } else {
                return false;
            }
        }

        return false;
    }

    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.isSource() && fluidState.getType() == Fluids.WATER;
        return super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl);
    }
}
