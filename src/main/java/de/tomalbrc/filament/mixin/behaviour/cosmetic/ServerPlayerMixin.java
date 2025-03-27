package de.tomalbrc.filament.mixin.behaviour.cosmetic;

import de.tomalbrc.filament.api.event.FilamentCosmeticEvents;
import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.cosmetic.CosmeticUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerMixin implements CosmeticInterface {
    @Inject(method = "initInventoryMenu", at = @At(value = "TAIL"))
    private void filament$onInitInventoryMenu(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            var slots = EquipmentSlotGroup.ARMOR.slots();
            for (EquipmentSlot slot : slots) {
                if (slot == EquipmentSlot.HEAD) {
                    continue;
                }

                var item = serverPlayer.getItemBySlot(slot);
                if (!item.isEmpty() && CosmeticUtil.isCosmetic(item)) {
                    filament$destroyHolder(serverPlayer.getEquipmentSlotForItem(item).getName());
                    FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), item, ItemStack.EMPTY);
                    filament$addHolder(serverPlayer, item.getItem(), item, serverPlayer.getEquipmentSlotForItem(item).getName());
                    FilamentCosmeticEvents.EQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), ItemStack.EMPTY, item);
                }
            }
        }
    }

    @Inject(method = "hasChangedDimension", at = @At("TAIL"))
    private void filament$onChangeDimension(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            var slots = EquipmentSlotGroup.ARMOR.slots();
            for (EquipmentSlot slot : slots) {
                if (slot == EquipmentSlot.HEAD) {
                    continue;
                }

                var item = serverPlayer.getItemBySlot(slot);

                if (!item.isEmpty() && CosmeticUtil.isCosmetic(item)) {
                    filament$destroyHolder(serverPlayer.getEquipmentSlotForItem(item).getName());
                    FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), item, ItemStack.EMPTY);
                    filament$addHolder(serverPlayer, item.getItem(), item, serverPlayer.getEquipmentSlotForItem(item).getName());
                    FilamentCosmeticEvents.EQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), ItemStack.EMPTY, item);
                }
            }
        }
    }
}
