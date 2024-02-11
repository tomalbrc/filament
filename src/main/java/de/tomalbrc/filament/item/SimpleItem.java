package de.tomalbrc.filament.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import de.tomalbrc.filament.data.ItemData;

import java.util.List;

public class SimpleItem extends Item implements PolymerItem, Equipable {
    final protected ItemData itemData;
    final protected Object2ObjectOpenHashMap<String, PolymerModelData> modelData;

    @Nullable
    PolymerArmorModel armorModel = null;

    public SimpleItem(Properties properties, ItemData itemData) {
        super(properties);
        this.itemData = itemData;
        this.modelData = this.itemData.requestModels();

        // For armor
        if (this.itemData.behaviour() != null && this.itemData.behaviour().armor != null && this.itemData.behaviour().armor.texture != null) {
            this.armorModel = PolymerResourcePackUtils.requestArmor(this.itemData.behaviour().armor.texture);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (itemData.properties() != null)
            itemData.properties().appendHoverText(tooltip);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayer player) {
        return itemData.vanillaItem() != null ? itemData.vanillaItem() : Items.PAPER;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        return modelData != null ? this.modelData.get("default").value() : -1;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player) {
        return armorModel != null ? this.armorModel.color() : -1;
    }

    public EquipmentSlot getEquipmentSlot() {
        if (itemData.behaviour() != null && itemData.behaviour().armor != null && itemData.behaviour().armor.slot != null) {
            return itemData.behaviour().armor.slot;
        }
        return EquipmentSlot.MAINHAND;
    }
}
