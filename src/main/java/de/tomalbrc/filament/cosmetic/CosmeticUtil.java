package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviours.item.Cosmetic;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CosmeticUtil {
    public static boolean isCosmetic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem && getCosmeticData(simpleItem) != null) {
            return true;
        }

        return false;
    }

    public static Cosmetic.CosmeticConfig getCosmeticData(ItemStack itemStack) {
        return getCosmeticData(itemStack.getItem());
    }

    public static Cosmetic.CosmeticConfig getCosmeticData(Item item) {
        Cosmetic.CosmeticConfig cosmeticData = null;
        if (item instanceof SimpleItem simpleItem && simpleItem.get(BehaviourRegistry.COSMETIC) != null) {
            cosmeticData = (Cosmetic.CosmeticConfig) simpleItem.get(BehaviourRegistry.COSMETIC).getConfig();
        }
        return cosmeticData;
    }
}
