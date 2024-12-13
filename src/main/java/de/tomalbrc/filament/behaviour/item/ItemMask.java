package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemMask implements ItemBehaviour<ItemMask.Config> {
    private final Config config;

    public ItemMask(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ItemMask.Config getConfig() {
        return this.config;
    }
    public static class Config {

    }
}
