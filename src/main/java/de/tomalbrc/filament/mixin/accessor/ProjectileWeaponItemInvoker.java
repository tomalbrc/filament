package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public interface ProjectileWeaponItemInvoker {
    @Invoker
    static List<ItemStack> invokeDraw(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity) {
        throw new AssertionError();
    }
}
