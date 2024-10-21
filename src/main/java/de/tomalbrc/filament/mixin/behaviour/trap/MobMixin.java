package de.tomalbrc.filament.mixin.behaviour.trap;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Trap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {
    @Inject(at= @At("HEAD"), method = "interact", cancellable = true)
    public void filament$trapItemInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        Mob mob = Mob.class.cast(this);
        if (itemStack.getItem() instanceof BehaviourHolder behaviourHolder && behaviourHolder.getBehaviours().has(Behaviours.TRAP) && !player.getCooldowns().isOnCooldown(itemStack.getItem())) {
            Trap trap = behaviourHolder.get(Behaviours.TRAP);
            boolean canUse = trap.canUseOn(mob);

            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > 1 && canUse && trap.canSave(mob)) {
                trap.saveToTag(mob, itemStack);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.WHITE_ASH, mob.position().x, mob.position().y, mob.position().z, 20, 0.125, 0.25, 0.125, 0.1);
                }

                mob.discard();
            }

            itemStack.getItem().use(player.level(), player, interactionHand);

            cir.setReturnValue(InteractionResult.CONSUME);
            cir.cancel();
        }
    }
}
