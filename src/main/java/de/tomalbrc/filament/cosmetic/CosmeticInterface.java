package de.tomalbrc.filament.cosmetic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface CosmeticInterface {
    void filament$addHolder(ServerPlayer serverPlayer, Item item, ItemStack itemStack);

    void filament$destroyHolder();
}
