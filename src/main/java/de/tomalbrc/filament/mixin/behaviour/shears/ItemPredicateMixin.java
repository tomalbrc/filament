package de.tomalbrc.filament.mixin.behaviour.shears;

import de.tomalbrc.filament.util.mixin.ItemPredicateCustomCheck;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin implements ItemPredicateCustomCheck {
    @Unique Predicate<ItemStack> filament$customCheck;

    @Override
    public void setCustomCheck(Predicate<ItemStack> itemStackPredicate) {
        filament$customCheck = itemStackPredicate;
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void filament$shearsCheck(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (filament$customCheck != null && !cir.getReturnValue()) {
            cir.setReturnValue(filament$customCheck.test(itemStack));
        }
    }
}
