package de.tomalbrc.filament.mixin.behaviour.trident;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ThrownTrident.class})
public interface ThrownTridentAccessor {
    @Accessor
    static EntityDataAccessor<Byte> getID_LOYALTY() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getID_FOIL() {
        throw new UnsupportedOperationException();
    }
}