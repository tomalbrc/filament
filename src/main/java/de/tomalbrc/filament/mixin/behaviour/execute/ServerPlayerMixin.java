package de.tomalbrc.filament.mixin.behaviour.execute;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.ExecuteAttackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ServerPlayerMixin {
    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    @Inject(method = "swingAndResetAttackStrength", at = @At("HEAD"))
    private void filament$handleSwing(final InteractionHand hand, final SwingAnimation animation, final boolean sendToSwingingEntity, CallbackInfo ci) {
        if (!((Object)this instanceof ServerPlayer player)) {
            return;
        }

        ItemStack itemStack = getItemInHand(hand);
        if (itemStack.getItem().isFilamentItem()) {
            ExecuteAttackItem ex = itemStack.getItem().get(Behaviours.ITEM_ATTACK_EXECUTE);
            if (ex != null && !ex.getConfig().onEntityAttack) {
                ex.runCommandItem(player, itemStack.getItem(), hand);
            }
        }
    }
}