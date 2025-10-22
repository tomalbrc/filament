package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.gui.PaginatedContainerGui;
import de.tomalbrc.filament.gui.VirtualChestMenu;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.mixin.RegistryUnfreezer;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.core.component.DataComponents.ITEM_MODEL;

public class Util {
    public static final SegmentedAnglePrecision SEGMENTED_ANGLE8 = new SegmentedAnglePrecision(3); // 3 bits precision = 8

    public static ResourceLocation id(String s) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, s);
    }

    public static Optional<Integer> validateAndConvertHexColor(String hexColor) {
        var res = ColorRGBA.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(hexColor));
        if (res.isSuccess()) {
            return Optional.of(res.getOrThrow().getFirst().rgba());
        }
        return Optional.empty();
    }

    public static void spawnAtLocation(Level level, Vec3 pos, ItemStack itemStack) {
        if (!itemStack.isEmpty() && !level.isClientSide()) {
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

        ((RegistryUnfreezer) BuiltInRegistries.BLOCK).filament$freeze();
        ((RegistryUnfreezer) BuiltInRegistries.ITEM).filament$freeze();
        ((RegistryUnfreezer) BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$freeze();
        ((RegistryUnfreezer) BuiltInRegistries.ENTITY_TYPE).filament$freeze();
        ((RegistryUnfreezer) BuiltInRegistries.CREATIVE_MODE_TAB).filament$freeze();

        ((PolymerIdList<?>) Block.BLOCK_STATE_REGISTRY).polymer$reorderEntries();
        ((PolymerIdList<?>) Fluid.FLUID_STATE_REGISTRY).polymer$reorderEntries();
    }

    @Deprecated(forRemoval = true)
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
        List<ResourceLocation> comps = List.of(
                Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.JUKEBOX_PLAYABLE)),
                Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.ENCHANTMENTS)),
                Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.TRIM)),
                Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.INSTRUMENT)),
                Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.BANNER_PATTERNS))
        );

        if (element.getAsJsonObject().has("components")) {
            JsonObject comp = element.getAsJsonObject().get("components").getAsJsonObject();
            for (String key : comp.keySet()) {
                comps.stream().filter(x -> x.toString().equals(key) || x.getPath().equals(key)).findAny().ifPresent(compId -> {
                    data.putAdditional(BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(compId), comp.get(key));
                });
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

    public static AbstractContainerMenu createMenu(Container container, int id, Inventory inventory, Player player) {
        return createMenu(container, id, inventory, player, false);
    }

    public static AbstractContainerMenu createMenu(Container container, int id, Inventory inventory, Player player, boolean forceCustom) {
        MenuType<?> menuType = forceCustom ? null : de.tomalbrc.filament.behaviour.decoration.Container.getMenuType(container.getContainerSize());

        if (menuType == null) {
            var est = de.tomalbrc.filament.behaviour.decoration.Container.estimateMenuType(container.getContainerSize());
            return new VirtualChestMenu(est, id, new PaginatedContainerGui(est, (ServerPlayer) player, false, container), player, container);
        }

        return new ChestMenu(menuType, id, inventory, container, container.getContainerSize() / 9) {
            @Override
            public void addChestGrid(Container container, int i, int j) {
                boolean doCheck = FilamentContainer.isPickUpContainer(container);
                for (int y = 0; y < this.getRowCount(); ++y) {
                    for (int x = 0; x < 9; ++x) {
                        this.addSlot(new Slot(container, x + y * 9, i + x * 18, j + y * 18) {
                            @Override
                            public boolean mayPlace(ItemStack itemStack) {
                                if (doCheck) {
                                    return itemStack.getItem().canFitInsideContainerItems();
                                }
                                return super.mayPlace(itemStack);
                            }
                        });
                    }
                }

            }
        };
    }

    public static void clickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5f, 1F);
    }
}