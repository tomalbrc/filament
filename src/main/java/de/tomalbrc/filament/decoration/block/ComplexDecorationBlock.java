package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ComplexDecorationBlock extends DecorationBlock implements EntityBlock {
    public ComplexDecorationBlock(Properties properties, ResourceLocation decorationId) {
        super(properties, decorationId);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DecorationBlockEntity(blockPos, blockState);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = levelReader.getBlockEntity(blockPos);
        ItemStack stack = blockEntity instanceof DecorationBlockEntity decorationBlockEntity ? decorationBlockEntity.getItem().copyWithCount(1) : super.getCloneItemStack(levelReader, blockPos, blockState);
        return stack;
    }
}
