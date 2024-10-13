package de.tomalbrc.filament.mixin.behaviour.bow;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow @Final Inventory inventory;

    @Shadow @Final private Abilities abilities;

    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void filament$onGetProjectile(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.BOW)) {
            Predicate<ItemStack> predicate = Objects.requireNonNull(simpleItem.get(Behaviours.BOW)).getSupportedHeldProjectiles();
            ItemStack projectile = ProjectileWeaponItem.getHeldProjectile(Player.class.cast(this), predicate);
            if (!projectile.isEmpty()) {
                cir.setReturnValue(projectile);
            } else {
                predicate = Objects.requireNonNull(simpleItem.get(Behaviours.BOW)).supportedProjectiles();
                var predicate2 = Objects.requireNonNull(simpleItem.get(Behaviours.CROSSBOW)).supportedProjectiles();
                for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                    ItemStack itemStack3 = this.inventory.getItem(i);
                    if (predicate.test(itemStack3) || predicate2.test(itemStack3)) {
                        cir.setReturnValue(itemStack3);
                        return;
                    }
                }

                cir.setReturnValue(this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private Predicate<ItemStack> filament$getPred(ItemStack itemStack) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.BOW)) {
            return simpleItem.get(Behaviours.BOW).supportedProjectiles();
        }
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.CROSSBOW)) {
            return simpleItem.get(Behaviours.CROSSBOW).supportedProjectiles();
        }

        return null;
    }
}
