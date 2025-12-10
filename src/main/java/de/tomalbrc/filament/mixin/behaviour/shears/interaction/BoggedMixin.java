package de.tomalbrc.filament.mixin.behaviour.shears.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.tomalbrc.filament.behaviour.item.Shears;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bogged.class)
public class BoggedMixin {
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean filament$customShears(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || Shears.is(instance);
    }
}
