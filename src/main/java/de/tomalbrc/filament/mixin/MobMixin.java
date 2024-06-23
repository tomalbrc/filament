package de.tomalbrc.filament.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import de.tomalbrc.filament.item.TrapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {
    @Inject(at= @At("HEAD"), method = "interact", cancellable = true)
    public void filament$trapItemInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        Mob mob = (Mob)(Object)this;

        if (itemStack.getItem() instanceof TrapItem trapItem) {
            boolean canUse = trapItem.canUseOn(mob);

            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > 1 && canUse && trapItem.canSave(mob)) {
                trapItem.saveToTag(mob, itemStack);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.WHITE_ASH, mob.position().x, mob.position().y, mob.position().z, 20, 0.125, 0.25, 0.125, 0.1);
                }

                mob.discard();
            }

            trapItem.use(player, interactionHand);

            cir.setReturnValue(InteractionResult.CONSUME);
            cir.cancel();
        }
    }
}
