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

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        CustomData customData;
        if (itemStack.has(DataComponents.CUSTOM_DATA) && (customData = itemStack.get(DataComponents.CUSTOM_DATA)) != null) {
            var tag = customData.copyTag();
            var originalItem = ItemStack.parseOptional(tooltipContext.registries(), tag.getCompound("Item"));
            list.add(Component.literal("---"));
            list.add(Component.literal("Masked Item:"));
            list.add(Component.literal("---"));
            originalItem.getItem().appendHoverText(originalItem, tooltipContext, list, tooltipFlag);
        }
    }

    public static ItemStack getMasked(RegistryAccess registryAccess, ItemStack original, ItemStack masker) {
        Tag tag = original.save(registryAccess);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("Item", tag);
        masker.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));

        return masker;
    }

    public static ItemStack getOriginal(RegistryAccess registryAccess, ItemStack masker) {
        CustomData customData = masker.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            return ItemStack.parseOptional(registryAccess, tag.getCompound("Item"));
        }

        return ItemStack.EMPTY;
    }

    public static class Config {

    }
}
