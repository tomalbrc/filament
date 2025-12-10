package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor
    double getBaseDamage();

    @Accessor
    void setFiredFromWeapon(ItemStack weapon);
}
