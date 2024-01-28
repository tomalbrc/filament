package de.tomalbrc.filament.decoration;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecorationBlock extends Block implements PolymerBlock, EntityBlock {
    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty PASSTHROUGH = BooleanProperty.create("passthrough");

    public DecorationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(PASSTHROUGH, false));
    }

    @Override
    public boolean forceLightUpdates(BlockState blockState) { return blockState.getValue(LIGHT_LEVEL) > 0; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, PASSTHROUGH);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return state.getValue(DecorationBlock.PASSTHROUGH) ? Blocks.AIR : Blocks.BARRIER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).defaultBlockState();
    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        super.wasExploded(level, blockPos, explosion);

        this.removeDecoration(level, blockPos, null);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DecorationBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(PASSTHROUGH)) {
            return Shapes.empty();
        } else {
            return Shapes.block();
        }
    }
}
