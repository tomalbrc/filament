package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public interface DecorationBehaviour<T> extends Behaviour<T> {
    default void init(DecorationBlockEntity blockEntity) {
    }

    default ElementHolder createHolder(DecorationBlockEntity blockEntity) {
        return null;
    }

    default void onElementAttach(DecorationBlockEntity blockEntity, ElementHolder holder) {
    }

    default InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        return InteractionResult.PASS;
    }

    default void read(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
    }

    default void write(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
    }

    default void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
    }

    default void modifyDrop(DecorationBlockEntity decorationBlockEntity, ItemStack itemStack) {
    }

    default ItemStack getCloneItemStack(ItemStack stack, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return stack;
    }
}