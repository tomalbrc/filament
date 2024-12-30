package de.tomalbrc.filament.mixin.behaviour.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin implements CosmeticInterface {
    @ModifyExpressionValue(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean filament$elytraUpdateFallFlying(boolean original, @Local ItemStack itemStack) {
        return original || itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.ELYTRA);
    }
}
