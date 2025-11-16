package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class Lever implements BlockBehaviour<Lever.Config> {
    private final Config config;

    public Lever(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Lever.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        this.cycle(blockState, level, blockPos);
        return InteractionResult.SUCCESS;
    }

    public void onExplosionHit(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (explosion.canTriggerBlocks()) {
            this.cycle(blockState, serverLevel, blockPos);
        }
    }

    private void cycle(BlockState blockState, Level level, BlockPos blockPos) {
        blockState = blockState.cycle(LeverBlock.POWERED);
        level.setBlock(blockPos, blockState, Block.UPDATE_ALL);
        this.updateNeighbours(blockState, level, blockPos);
        playSound(null, level, blockPos, blockState);
        level.gameEvent(null, blockState.getValue(LeverBlock.POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockPos);
    }

    protected void playSound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        float pitch = config.pitch.getOrDefault(blockState, blockState.getValue(LeverBlock.POWERED) ? 0.6F : 0.5F);
        levelAccessor.playSound(player, blockPos, SoundEvent.createVariableRangeEvent(config.sound.getValue(blockState)), SoundSource.BLOCKS, config.volume.getValue(blockState), pitch);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, Level level, BlockPos pos, BlockState blockState2, boolean bl) {
        if (!bl && state.getValue(LeverBlock.POWERED)) {
            this.updateNeighbours(state, level, pos);
        }

    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(LeverBlock.POWERED) ? config.powerlevel.getValue(blockState) : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(LeverBlock.POWERED) && getConnectedDirection(blockState) == direction ? config.powerlevel.getValue(blockState) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, blockState.getBlock());
        level.updateNeighborsAt(blockPos.relative(getConnectedDirection(blockState).getOpposite()), blockState.getBlock());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LeverBlock.FACE, LeverBlock.FACING, LeverBlock.POWERED);
    }

    static Direction getConnectedDirection(BlockState state) {
        return switch (state.getValue(LeverBlock.FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> state.getValue(LeverBlock.FACING);
        };
    }

    public static class Config {
        public BlockStateMappedProperty<Integer> powerlevel = BlockStateMappedProperty.of(15);
        public BlockStateMappedProperty<ResourceLocation> sound = BlockStateMappedProperty.of(SoundEvents.LEVER_CLICK.getLocation());
        public BlockStateMappedProperty<Float> volume = BlockStateMappedProperty.of(0.3f);
        public BlockStateMappedProperty<Float> pitch = BlockStateMappedProperty.empty();
    }
}