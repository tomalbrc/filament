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

@Mixin(ItemPredicate.Builder.class)
public class ItemPredicateBuilderMixin implements ItemPredicateCustomCheck {
    @Unique Predicate<ItemStack> filament$customCheck;

    @Override
    public void setCustomCheck(Predicate<ItemStack> itemStackPredicate) {
        filament$customCheck = itemStackPredicate;
    }

    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void filament$shearsCheck(CallbackInfoReturnable<ItemPredicate> cir) {
        if (filament$customCheck != null) {
            var pred = cir.getReturnValue();
            if (pred != null && (Object)pred instanceof ItemPredicateCustomCheck customCheck) {
                customCheck.setCustomCheck(filament$customCheck);
                cir.setReturnValue(pred);
            }
        }
    }
}
