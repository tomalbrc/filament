package de.tomalbrc.filament.mixin.behaviour.shears;

import de.tomalbrc.filament.util.mixin.ItemPredicateCustomCheck;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin implements ItemPredicateCustomCheck {
    @Unique Predicate<ItemInstance> filament$customCheck;

    @Override
    public void setCustomCheck(Predicate<ItemInstance> itemStackPredicate) {
        filament$customCheck = itemStackPredicate;
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemInstance;)Z", at = @At("RETURN"), cancellable = true)
    private void filament$shearsCheck(ItemInstance itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (filament$customCheck != null && !cir.getReturnValue()) {
            cir.setReturnValue(filament$customCheck.test(itemStack));
        }
    }
}
