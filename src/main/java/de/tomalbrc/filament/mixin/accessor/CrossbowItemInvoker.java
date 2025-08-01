package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemInvoker {
    @Invoker
    static float invokeGetPowerForTime(int i, ItemStack itemStack, LivingEntity livingEntity) {
        throw new AssertionError();
    }
}
