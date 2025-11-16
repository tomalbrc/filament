package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public interface DecorationBehaviour<T> extends Behaviour<T> {
    default void init(DecorationBlockEntity blockEntity) {
    }

    default FilamentDecorationHolder createHolder(DecorationBlockEntity blockEntity) {
        return null;
    }

    default void onHolderAttach(DecorationBlockEntity blockEntity, FilamentDecorationHolder holder) {
    }

    default InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        return InteractionResult.PASS;
    }

    default void read(CompoundTag output, HolderLookup.Provider lookup, DecorationBlockEntity blockEntity) {
    }

    default void write(CompoundTag input, HolderLookup.Provider lookup, DecorationBlockEntity blockEntity) {
    }

    default void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
    }

    default void modifyDrop(DecorationBlockEntity decorationBlockEntity, ItemStack itemStack) {
    }

    // Allows to change the visual item stack
    default ItemStack visualItemStack(DecorationBlockEntity decorationBlockEntity, ItemStack adjusted, BlockState blockState) {
        return adjusted;
    }

    default BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState;
    }

    default ItemStack getCloneItemStack(ItemStack stack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return stack;
    }

    default void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, BlockEntity.DataComponentInput dataComponentGetter) {}

    default void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {}

    default void removeComponentsFromTag(DecorationBlockEntity decorationBlockEntity, CompoundTag valueOutput, HolderLookup.Provider lookup) {}

    default void postBreak(DecorationBlockEntity decorationBlockEntity, BlockPos blockPos, Player player) {

    }
}