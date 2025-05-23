package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.world.item.ItemStack;

public class CosmeticUtil {
    public static boolean isCosmetic(ItemStack itemStack) {
        return getCosmeticData(itemStack) != null;
    }

    public static Cosmetic.Config getCosmeticData(ItemStack item) {
        Cosmetic.Config cosmeticData = null;
        if (item.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.COSMETIC)) {
            cosmeticData = filamentItem.get(Behaviours.COSMETIC).getConfig();
        }
        if (item.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var wrapped = item.get(FilamentComponents.SKIN_DATA_COMPONENT);
            if (wrapped != null && wrapped.getItem() instanceof FilamentItem wrappedItem && wrappedItem.has(Behaviours.COSMETIC)) {
                cosmeticData = wrappedItem.get(Behaviours.COSMETIC).getConfig();
            }
        }
        return cosmeticData;
    }
}
