package de.tomalbrc.filament.mixin.trim;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.FilamentTrimPatterns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/item/ItemStack$1", priority = 400)
public abstract class ItemStackPacketCodecMixin {
    @Inject(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void filament$modifyVanillaItem(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack, CallbackInfo ci, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        ServerPlayer player = PolymerUtils.getPlayerContext();
        boolean isPolymerStack = itemStack.getItem() instanceof PolymerItem;
        if (!isPolymerStack && !FilamentTrimPatterns.isEmpty() && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(ArmorMaterials.CHAIN))
            itemStack.set(DataComponents.TRIM, new ArmorTrim(player.registryAccess().lookup(Registries.TRIM_MATERIAL).get().get(TrimMaterials.REDSTONE).get(), FilamentTrimPatterns.CHAIN_TRIM.trimPattern, false));
    }
}