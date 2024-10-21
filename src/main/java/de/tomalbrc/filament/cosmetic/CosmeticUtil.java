package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CosmeticUtil {
    public static boolean isCosmetic(ItemStack itemStack) {
        return itemStack.getItem() instanceof SimpleItem simpleItem && getCosmeticData(simpleItem) != null;
    }

    public static Cosmetic.Config getCosmeticData(ItemStack itemStack) {
        return getCosmeticData(itemStack.getItem());
    }

    public static Cosmetic.Config getCosmeticData(Item item) {
        Cosmetic.Config cosmeticData = null;
        if (item instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.COSMETIC)) {
            cosmeticData = simpleItem.get(Behaviours.COSMETIC).getConfig();
        }
        return cosmeticData;
    }
}
