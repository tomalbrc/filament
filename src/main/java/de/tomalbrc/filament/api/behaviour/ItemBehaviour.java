package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviours.BehaviourHolder;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface ItemBehaviour<T> extends Behaviour<T> {
    default void init(Item item, BehaviourHolder behaviourHolder) {
    }

    default void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
    }

    default InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    default InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    default ItemStack modifyPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        return itemStack;
    }

    default int modifyPolymerCustomModelData(Map<String, PolymerModelData> modelData, ItemStack itemStack, @Nullable ServerPlayer player) {
        return -1;
    }

    default int modifyPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player, int color) {
        return color;
    }

    default EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.MAINHAND;
    }
}
