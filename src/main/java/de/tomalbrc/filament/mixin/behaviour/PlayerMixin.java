package de.tomalbrc.filament.mixin.behaviour;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack bl4(ItemStack original) {
        if (original.getItem() instanceof SimpleItem simpleItem && simpleItem.components().has(DataComponents.TOOL)) {
            return Items.WOODEN_SWORD.getDefaultInstance();
        }
        return original;
    }
}
