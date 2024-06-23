package de.tomalbrc.filament.mixin.cosmetic;

import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerMixin implements CosmeticInterface {
    @Inject(method = "initInventoryMenu", at = @At(value = "TAIL"))
    private void filament$onReadAdditionalSaveData(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            var item = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
            if (!item.isEmpty() && item.getItem() instanceof SimpleItem simpleItem && simpleItem.getItemData().isCosmetic()) {
                filament$addHolder(serverPlayer, simpleItem, item);
            }
        }
    }
}
