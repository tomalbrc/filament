package de.tomalbrc.filament.mixin.component.skin;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "overrideStackedOnOther", at = @At(value = "HEAD"), cancellable = true)
    private void filament$stackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!slot.allowModification(player)) return;

        var itemStack2 = slot.getItem();
        // take out by clicking empty slot
        if (!itemStack2.isEmpty() && !itemStack2.has(FilamentComponents.SKIN_DATA_COMPONENT) && !itemStack.isEmpty() && (itemStack.has(FilamentComponents.SKIN_COMPONENT) && itemStack2.is(itemStack.get(FilamentComponents.SKIN_COMPONENT)))) { // overlay / add as skin
            itemStack2.set(FilamentComponents.SKIN_DATA_COMPONENT, itemStack.copyWithCount(1));

            if (filament$isWearing(player, itemStack2)) {
                player.onEquipItem(player.getEquipmentSlotForItem(itemStack2), itemStack, itemStack2);
            }

            itemStack.consume(1, player);

            cir.setReturnValue(itemStack.isEmpty());
        } else if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty() && itemStack.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            slot.set(itemStack.get(FilamentComponents.SKIN_DATA_COMPONENT));
            var old = itemStack.remove(FilamentComponents.SKIN_DATA_COMPONENT);

            if (filament$isWearing(player, itemStack)) {
                player.onEquipItem(player.getEquipmentSlotForItem(itemStack), old, itemStack2);
            }

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At(value = "HEAD"), cancellable = true)
    private void filament$stackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, CallbackInfoReturnable<Boolean> cir) {
        if (!slot.allowModification(player)) return;

        if (clickAction == ClickAction.SECONDARY) {
            // insert or swap

            if (itemStack.has(FilamentComponents.SKIN_DATA_COMPONENT) && itemStack2.isEmpty()) {
                // Remove skin
                var stack = itemStack.get(FilamentComponents.SKIN_DATA_COMPONENT).copy();
                var old = itemStack.remove(FilamentComponents.SKIN_DATA_COMPONENT);

                if (filament$isWearing(player, itemStack)) {
                    player.onEquipItem(player.getEquipmentSlotForItem(itemStack), old, itemStack2);
                }

                slotAccess.set(stack);
                cir.setReturnValue(true);
            } else if (!itemStack.isEmpty() && (itemStack.has(FilamentComponents.SKIN_COMPONENT) && itemStack2.is(itemStack.get(FilamentComponents.SKIN_COMPONENT)))) {
                if (itemStack2.has(FilamentComponents.SKIN_DATA_COMPONENT) && itemStack2.getCount() == 1 && itemStack.getCount() == 1) {
                    // swap skin of slot with that on the carried item
                    var itemStack2Copy = itemStack2.copy();
                    var old = itemStack2Copy.get(FilamentComponents.SKIN_DATA_COMPONENT).copy();
                    itemStack2Copy.set(FilamentComponents.SKIN_DATA_COMPONENT, itemStack.copyWithCount(1));
                    slot.set(old);
                    slotAccess.set(itemStack2Copy);

                    if (filament$isWearing(player, itemStack2Copy)) {
                        player.onEquipItem(player.getEquipmentSlotForItem(itemStack2Copy), old, itemStack2Copy);
                    }

                    cir.setReturnValue(true);
                } else {
                    // just insert (the item to skin "picks up" the skin by
                    // right-clicking a skin with the item to skin in the cursor slot
                    var old = itemStack2.set(FilamentComponents.SKIN_DATA_COMPONENT, itemStack.copyWithCount(1));
                    if (filament$isWearing(player, itemStack)) {
                        player.onEquipItem(player.getEquipmentSlotForItem(itemStack), old, itemStack2);
                    }

                    itemStack.setCount(itemStack.getCount()-1);
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Unique
    private static boolean filament$isWearing(Player player, ItemStack itemStack) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack == itemStack) {
                return true;
            }
        }
        return false;
    }
}
