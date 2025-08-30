package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Furnace implements BlockBehaviourWithEntity<Furnace.Config> {
    private final Config config;

    public Furnace(Config config) {
        this.config = config;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FurnaceBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.LIT, false);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    @Nullable
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level level, BlockState blockState1, BlockEntityType<A> blockEntityType) {
        BlockEntityTicker<A> ticker;
        if (level instanceof ServerLevel serverLevel) {
            ticker = BlockBehaviourWithEntity.createTickerHelper(blockEntityType, BlockEntityType.FURNACE, (levelx, blockPos, blockState, abstractFurnaceBlockEntity) -> AbstractFurnaceBlockEntity.serverTick(serverLevel, blockPos, blockState, abstractFurnaceBlockEntity));
        } else {
            ticker = null;
        }
        return ticker;
    }

    @Override
    public BlockEntityType<?> blockEntityType() {
        return BlockEntityType.FURNACE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof FurnaceBlockEntity) {
                player.openMenu((MenuProvider)blockEntity);
                player.awardStat(Stats.INTERACT_WITH_FURNACE);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    public Optional<Boolean> hasAnalogOutputSignal(BlockState blockState) {
        return Optional.of(true);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    @Override
    @NotNull
    public Furnace.Config getConfig() {
        return this.config;
    }
    public static class Config {

    }
}