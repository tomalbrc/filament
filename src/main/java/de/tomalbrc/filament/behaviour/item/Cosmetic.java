package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic implements ItemBehaviour<Cosmetic.Config> {
    private final Config config;

    public Cosmetic(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Cosmetic.Config getConfig() {
        return this.config;
    }


    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (!config.mask) return;

        CustomData customData;
        if (itemStack.has(DataComponents.CUSTOM_DATA)) {
            var originalItem = getOriginal(tooltipContext.registries(), itemStack);
            list.add(Component.literal("---"));
            list.add(Component.literal("Masked Item:"));
            list.add(Component.literal("---"));
            list.add(originalItem.getItem().getName());
            list.add(Component.empty());
            originalItem.getItem().appendHoverText(originalItem, tooltipContext, list, tooltipFlag);
        }
    }

    public static void updateMaskedData(HolderLookup.Provider lookupProvider, ItemStack original, ItemStack masker) {
        Tag tag = original.save(lookupProvider);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("Item", tag);
        masker.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));

    }

    public static ItemStack getOriginal(HolderLookup.Provider lookupProvider, ItemStack masker) {
        CustomData customData = masker.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            return ItemStack.parseOptional(lookupProvider, tag.getCompound("Item"));
        }

        return ItemStack.EMPTY;
    }

    public static class Config {
        /**
         * The equipment slot for the cosmetic
         */
        public EquipmentSlot slot;

        public boolean mask = true;

        /**
         * The resource location of the animated model for the cosmetic.
         */
        public ResourceLocation model;

        /**
         * The name of the animation to autoplay. The animation should be loopable
         */
        public String autoplay;

        /**
         * Scale of the chest cosmetic
         */
        public Vector3f scale = new Vector3f(1);

        /**
         * Translation of the chest cosmetic
         */
        public Vector3f translation = new Vector3f();
    }
}
