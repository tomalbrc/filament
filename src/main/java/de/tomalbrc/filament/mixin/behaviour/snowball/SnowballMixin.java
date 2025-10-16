package de.tomalbrc.filament.mixin.behaviour.snowball;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public abstract class SnowballMixin extends ThrowableItemProjectile {
    @Unique private boolean filament$didExec = false;

    public SnowballMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onHit", at = @At("RETURN"))
    private void filament$onHit(HitResult result, CallbackInfo ci) {
        if (!filament$didExec && this.getItem().getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.SNOWBALL)) {
            var b = simpleItem.getOrThrow(Behaviours.SNOWBALL);
            b.run(((Snowball)(Object)this), result.getLocation());
            filament$didExec = true;
        }
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void filament$onHitEntity(EntityHitResult result, CallbackInfo ci) {
        if (!filament$didExec && this.getItem().getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.SNOWBALL)) {
            var b = simpleItem.getOrThrow(Behaviours.SNOWBALL);
            b.runEntity(((Snowball)(Object)this), result.getLocation(), result.getEntity());
            filament$didExec = true;
        }
    }
}
