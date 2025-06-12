package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.item.FilamentItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.core.component.DataComponents.ITEM_MODEL;

public class Util {
    public static final SegmentedAnglePrecision SEGMENTED_ANGLE8 = new SegmentedAnglePrecision(3); // 3 bits precision = 8

    public static Optional<Integer> validateAndConvertHexColor(String hexColor) {
        // Regular expression pattern to match hex color strings
        Pattern hexColorPattern = Pattern.compile("^#?([A-Fa-f0-9]{6})$|^0x([A-Fa-f0-9]{6})$");

        Matcher matcher = hexColorPattern.matcher(hexColor);

        if (matcher.matches()) {
            String hexDigits = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            int intValue = Integer.parseInt(hexDigits, 16);
            return Optional.of(intValue);
        } else {
            Filament.LOGGER.warn("Invalid hex color formats");
            return Optional.empty();
        }
    }

    public static void spawnAtLocation(Level level, Vec3 pos, ItemStack itemStack) {
        if (!itemStack.isEmpty() && !level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public static void loadDatapackContents(ResourceManager resourceManager) {
        ((RegistryUnfreezer) BuiltInRegistries.BLOCK).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.ITEM).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.ENTITY_TYPE).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.CREATIVE_MODE_TAB).filament$unfreeze();

        for (SimpleSynchronousResourceReloadListener listener : FilamentReloadUtil.getReloadListeners()) {
            listener.onResourceManagerReload(resourceManager);
        }

        ((RegistryUnfreezer)BuiltInRegistries.BLOCK).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.ITEM).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.ENTITY_TYPE).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.CREATIVE_MODE_TAB).filament$freeze();
    }

    public static void damageAndBreak(int i, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot slot) {
        int newDamage = itemStack.getDamageValue() + i;
        itemStack.setDamageValue(newDamage);

        if (newDamage >= itemStack.getMaxDamage()) {
            Item item = itemStack.getItem();
            itemStack.shrink(1);
            livingEntity.onEquippedItemBroken(item, slot);
        }
    }

    public static void handleComponentsCustom(JsonElement element, Data<?> data) {
        if (element.getAsJsonObject().has("components")) {
            JsonObject comp = element.getAsJsonObject().get("components").getAsJsonObject();
            if (comp.has("minecraft:jukebox_playable")) {
                data.putAdditional(DataComponents.JUKEBOX_PLAYABLE, comp.get("minecraft:jukebox_playable"));
            }
            if (comp.has("jukebox_playable")) {
                data.putAdditional(DataComponents.JUKEBOX_PLAYABLE, comp.get("jukebox_playable"));
            }
        }
    }

    public static ItemStack filamentItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext packetContext, FilamentItem filamentItem) {
        ItemStack stack = PolymerItemUtils.createItemStack(itemStack, tooltipType, packetContext);

        ResourceLocation dataComponentModel = null;
        if (filamentItem.getData() != null && filamentItem.getData().components().has(ITEM_MODEL)) {
            dataComponentModel = filamentItem.getData().components().get(ITEM_MODEL);
        } else if (filamentItem.getData() != null) {
            if (filamentItem.getData().itemModel() != null) {
                dataComponentModel = filamentItem.getData().itemModel();
            } else {
                dataComponentModel = filamentItem.getData().preferredResource() == null ? filamentItem.getData().vanillaItem().components().get(ITEM_MODEL) : null;
            }
        }
        if (dataComponentModel != null) stack.set(ITEM_MODEL, dataComponentModel);

        filamentItem.getDelegate().modifyPolymerItemStack(filamentItem.getModelMap(), itemStack, stack, tooltipType, packetContext.getRegistryWrapperLookup(), packetContext.getPlayer());

        return stack;
    }
}