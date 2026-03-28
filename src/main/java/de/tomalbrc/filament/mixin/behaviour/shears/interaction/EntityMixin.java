package de.tomalbrc.filament.mixin.behaviour.shears.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.tomalbrc.filament.behaviour.item.Shears;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean filament$customShears(ItemStack instance, Object o, Operation<Boolean> original) {
        return original.call(instance, o) || (o == Items.SHEARS && Shears.is(instance));
    }
    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean filament$customShears2(ItemStack instance, Object o, Operation<Boolean> original) {
        return original.call(instance, o) || (o == Items.SHEARS && Shears.is(instance));
    }
}
