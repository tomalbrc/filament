package de.tomalbrc.filament.mixin.behaviour;

import de.tomalbrc.filament.registry.FuelRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

// Fuel behaviourConfig support
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Inject(method = "getFuel", at = @At("RETURN"))
    private static void getFuelCache(CallbackInfoReturnable<Map<Item, Integer>> cir) {
        cir.getReturnValue().putAll(FuelRegistry.getCache());
    }
}
