package de.tomalbrc.filament.mixin.behaviour.elytra;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract void startFallFlying();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tryToStartFallFlying", at = @At("RETURN"), cancellable = true)
    private void filament$elytraTryToStartFallFlying(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (!this.onGround() && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION)) {
                ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
                if (itemStack.getItem() instanceof FilamentItem simpleItem && simpleItem.has(Behaviours.ELYTRA) && ElytraItem.isFlyEnabled(itemStack)) {
                    this.startFallFlying();
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
