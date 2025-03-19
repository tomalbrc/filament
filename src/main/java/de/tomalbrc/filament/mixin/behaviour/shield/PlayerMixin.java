package de.tomalbrc.filament.mixin.behaviour.shield;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean filament$customShield(boolean original) {
        return original || this.getUseItem().getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.SHIELD);
    }
}
