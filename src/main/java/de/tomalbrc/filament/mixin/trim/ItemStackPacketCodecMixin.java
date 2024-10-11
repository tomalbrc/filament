package de.tomalbrc.filament.mixin.trim;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.trim.FilamentTrimPatterns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/item/ItemStack$1", priority = 400) // 400 so we can change it before polymer does
public abstract class ItemStackPacketCodecMixin {
    @Inject(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void filament$modifyChainmail(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack, CallbackInfo ci, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        if (FilamentTrimPatterns.overwriteChainMail()) {
            ServerPlayer player = PolymerUtils.getPlayerContext();
            boolean isPolymerStack = itemStack.getItem() instanceof PolymerItem;
            if (player != null && !isPolymerStack && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(ArmorMaterials.CHAIN))
                FilamentTrimPatterns.apply(player.registryAccess(), itemStack);
        }
    }
}