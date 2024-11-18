package de.tomalbrc.filament.cosmetic;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface CosmeticInterface {
    void filament$addHolder(LivingEntity livingEntity, Item item, ItemStack itemStack);

    void filament$destroyHolder(ItemStack itemStack);
}
