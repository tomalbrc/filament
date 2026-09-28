package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {
    @Invoker(value = "isSunBurnTick")
    boolean invokeIsSunBurnTick();
}
