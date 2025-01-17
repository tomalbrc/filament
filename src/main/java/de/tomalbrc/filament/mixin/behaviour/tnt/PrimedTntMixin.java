package de.tomalbrc.filament.mixin.behaviour.tnt;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.block.SimpleBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity {
    public PrimedTntMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract BlockState getBlockState();

    @Shadow private boolean usedPortal;

    @Shadow @Final private static ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR;

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void filament$customExplode(CallbackInfo ci) {
        if (this.getBlockState().getBlock() instanceof SimpleBlock simpleBlock && simpleBlock.has(Behaviours.TNT)) {
            var conf = Objects.requireNonNull(simpleBlock.get(Behaviours.TNT)).getConfig();
            this.level().explode(this, Explosion.getDefaultDamageSource(this.level(), this), this.usedPortal ? USED_PORTAL_DAMAGE_CALCULATOR : null, this.getX(), this.getY(0.0625F), this.getZ(), conf.explosionPower.getValue(getBlockState()), false, Level.ExplosionInteraction.TNT);
            ci.cancel();
        }
    }
}
