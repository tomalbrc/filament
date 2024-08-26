package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.behaviours.item.Cosmetic;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CosmeticUtil {
    public static boolean isCosmetic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof DecorationItem simpleItem && simpleItem.getDecorationData().isCosmetic()) {
            return true;
        }
        if (itemStack.getItem() instanceof SimpleBlockItem simpleItem && simpleItem.getBlockData().isCosmetic()) {
            return true;
        }
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.getItemData().isCosmetic()) {
            return true;
        }

        return false;
    }

    public static Cosmetic getCosmeticData(ItemStack itemStack) {
        return getCosmeticData(itemStack.getItem());
    }

    public static Cosmetic getCosmeticData(Item item) {
        Cosmetic cosmeticData = null;
        if (item instanceof DecorationItem simpleItem && simpleItem.getDecorationData().isCosmetic()) {
            cosmeticData = simpleItem.getDecorationData().behaviour().get(Constants.Behaviours.COSMETIC);
        }
        if (item instanceof SimpleBlockItem simpleItem && simpleItem.getBlockData().isCosmetic()) {
            cosmeticData = simpleItem.getBlockData().behaviour().get(Constants.Behaviours.COSMETIC);
        }
        if (item instanceof SimpleItem simpleItem && simpleItem.getItemData().isCosmetic()) {
            cosmeticData = simpleItem.getItemData().behaviour().get(Constants.Behaviours.COSMETIC);
        }

        return cosmeticData;
    }
}
