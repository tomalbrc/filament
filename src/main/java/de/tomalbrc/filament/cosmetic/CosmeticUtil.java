package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.world.item.ItemStack;

public class CosmeticUtil {
    public static boolean isCosmetic(ItemStack itemStack) {
        return getCosmeticData(itemStack) != null;
    }

    public static Cosmetic.Config getCosmeticData(ItemStack item) {
        Cosmetic.Config cosmeticData = null;
        if (item.getItem().isFilamentItem() && item.getItem().has(Behaviours.COSMETIC)) {
            cosmeticData = item.getItem().getOrThrow(Behaviours.COSMETIC).getConfig();
        }
        if (item.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var wrapped = item.get(FilamentComponents.SKIN_DATA_COMPONENT);
            if (wrapped != null && wrapped.getItem().isFilamentItem() && wrapped.getItem().has(Behaviours.COSMETIC)) {
                cosmeticData = wrapped.getItem().getOrThrow(Behaviours.COSMETIC).getConfig();
            }
        }
        return cosmeticData;
    }
}
