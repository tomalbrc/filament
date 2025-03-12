package de.tomalbrc.filament.mixin.component.skin;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract void hurtAndBreak(int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot);

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void filament$dontDestroySkin(int i, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci) {
        var self = ItemStack.class.cast(this);
        if (self.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var skinItem = self.get(FilamentComponents.SKIN_DATA_COMPONENT);
            if (skinItem != null && !skinItem.isEmpty()) {
                if (!serverPlayer.addItem(skinItem))
                    serverPlayer.spawnAtLocation(skinItem);

                self.remove(FilamentComponents.SKIN_DATA_COMPONENT);
            }
        }
    }
}
