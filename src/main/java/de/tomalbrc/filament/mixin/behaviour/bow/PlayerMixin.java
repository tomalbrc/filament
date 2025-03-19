package de.tomalbrc.filament.mixin.behaviour.bow;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.FilamentItem;
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

import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow @Final Inventory inventory;

    @Shadow @Final private Abilities abilities;

    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void filament$onGetProjectile(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (filament$bowOrCrossbow(itemStack)) {
            Predicate<ItemStack> predicate = filament$getHeldPred(itemStack);
            ItemStack projectile = ProjectileWeaponItem.getHeldProjectile(Player.class.cast(this), predicate);
            if (!projectile.isEmpty()) {
                cir.setReturnValue(projectile);
            } else {
                predicate = filament$getPred(itemStack);
                for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                    ItemStack itemStack3 = this.inventory.getItem(i);
                    if (predicate.test(itemStack3)) {
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
        if (itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.BOW)) {
            return filamentItem.get(Behaviours.BOW).supportedProjectiles();
        }
        if (itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.CROSSBOW)) {
            return filamentItem.get(Behaviours.CROSSBOW).supportedProjectiles();
        }

        return (itemStack1) -> false;
    }

    @Unique
    private Predicate<ItemStack> filament$getHeldPred(ItemStack itemStack) {
        if (itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.BOW)) {
            return filamentItem.get(Behaviours.BOW).supportedHeldProjectiles();
        }
        if (itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.CROSSBOW)) {
            return filamentItem.get(Behaviours.CROSSBOW).supportedHeldProjectiles();
        }

        return (itemStack1) -> false;
    }

    @Unique
    private boolean filament$bowOrCrossbow(ItemStack itemStack) {
        return itemStack.getItem() instanceof FilamentItem filamentItem && (filamentItem.has(Behaviours.BOW) || filamentItem.has(Behaviours.CROSSBOW));
    }
}
