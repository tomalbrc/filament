package de.tomalbrc.filament.mixin.behaviour.shears.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.tomalbrc.filament.behaviour.item.Shears;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TripWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TripWireBlock.class)
public class TripWireBlockMixin {
    @WrapOperation(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean filament$customShears(ItemStack instance, Object o, Operation<Boolean> original) {
        return original.call(instance, o) || Shears.is(instance);
    }
}
