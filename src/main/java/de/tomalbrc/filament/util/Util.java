package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.gui.PaginatedContainerGui;
import de.tomalbrc.filament.gui.VirtualChestMenu;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.mixin.RegistryUnfreezer;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import eu.pb4.polymer.core.impl.other.PacketTooltipContext;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static eu.pb4.polymer.core.api.item.PolymerItemUtils.POLYMER_COUNTED;
import static eu.pb4.polymer.core.api.item.PolymerItemUtils.POLYMER_STACK;
import static net.minecraft.core.component.DataComponents.ITEM_MODEL;

public class Util {
    public static final SegmentedAnglePrecision SEGMENTED_ANGLE8 = new SegmentedAnglePrecision(3); // 3 bits precision = 8

    public static Identifier id(String s) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, s);
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

        ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$reorderEntries();
        ((PolymerIdMapper<?>) Fluid.FLUID_STATE_REGISTRY).polymer$reorderEntries();
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
        List<Identifier> comps = List.of(
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
        ItemStack stack = createItemStack(itemStack, tooltipType, packetContext, Filament.SERVER.registryAccess());

        Identifier dataComponentModel = null;
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

        var fuckFabric = packetContext.get(PacketContext.GAME_PROFILE);
        filamentItem.getDelegate().modifyPolymerItemStack(filamentItem.getModelMap(), itemStack, stack, tooltipType, Filament.SERVER.registryAccess(), fuckFabric == null ? null : Filament.SERVER.getPlayerList().getPlayer(fuckFabric.id()));

        return stack;
    }

    // via polymer
    public static ItemStack createItemStack(ItemStack itemStack, TooltipFlag tooltipContext, PacketContext context, HolderLookup.Provider lookup) {
        Item item = itemStack.getItem();
        Identifier model = null;
        boolean storeCount;
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, context);
            item = data.item();
            storeCount = virtualItem.shouldStorePolymerItemStackCount();
            model = data.itemModel() != null ? data.itemModel() : item.components().get(DataComponents.ITEM_MODEL);
        } else {
            storeCount = false;
            model = itemStack.get(DataComponents.ITEM_MODEL);
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());
        for (var x : out.getComponents().keySet()) {
            if (itemStack.getComponents().get(x) == null) {
                out.set(x, null);
            }
        }

        if (model != null) {
            out.set(DataComponents.ITEM_MODEL, model);
        }

        for (var i = 0; i < COMPONENTS_TO_COPY.length; i++) {
            var key = COMPONENTS_TO_COPY[i];
            var x = itemStack.get(key);

            if (x instanceof TransformingComponent t) {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, t.polymer$getTransformed(context));
            } else {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, (Object) itemStack.get(key));
            }
        }

        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
            polymerItem.modifyBasePolymerItemStack(out, itemStack, context, lookup);
        }

        {
            var current = itemStack.get(DataComponents.USE_COOLDOWN);
            if (current == null) {
                out.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.00001f, Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))));
            } else if (current.cooldownGroup().isEmpty()) {
                out.set(DataComponents.USE_COOLDOWN, new UseCooldown(current.seconds(), Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))));
            }
        }


        out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, itemStack.hasFoil());


        // Set item name
        {
            var name = itemStack.getItemName();
            out.set(DataComponents.ITEM_NAME, name);

            if (!out.has(DataComponents.CUSTOM_NAME)) {
                if (
                        (item instanceof CompassItem && out.has(DataComponents.LODESTONE_TRACKER))
                                || ((item instanceof PotionItem || item instanceof TippedArrowItem) && out.has(DataComponents.POTION_CONTENTS))
                                || (item instanceof PlayerHeadItem && out.has(DataComponents.PROFILE) && Objects.requireNonNull(out.get(DataComponents.PROFILE)).name().isPresent())

                ) {
                    out.set(DataComponents.CUSTOM_NAME, Component.empty().append(name).setStyle(Style.EMPTY.withItalic(false)));
                }
            }
        }


        try {
            // Todo: Replace this once https://github.com/FabricMC/fabric-api/pull/5256 is merged!
            out.set(DataComponents.CUSTOM_DATA, PacketContext.supplyWithContext(NULL_CONTEXT_PROVIDER, () -> {
                var nbt = new CompoundTag();

                nbt.store(POLYMER_STACK, ItemStack.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), itemStack);

                if (storeCount) {
                    nbt.putBoolean(POLYMER_COUNTED, true);
                } else {
                    nbt.remove("count");
                }

                return CustomData.of(nbt);
            }));
        } catch (Throwable e) {
            var profile = context.get(PacketContext.GAME_PROFILE);
            Filament.LOGGER.error("Failed to encode Polymer item stack data {} for {}", itemStack, profile != null ? profile.name() : "<Unknown>", e);
        }


        var display = out.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        for (var x : out.getComponents()) {
            if (!IGNORE_TOOLTIP_HIDING.contains(x.type()) && (x.value() instanceof TooltipProvider || FORCE_HIDE_TOOLTIP.contains(x.type()))) {
                display = display.withHidden(x.type(), true);
            }
        }
        if (out.has(DataComponents.DAMAGE) && !itemStack.has(DataComponents.DAMAGE)) {
            display = display.withHidden(DataComponents.DAMAGE, true);
        }

        display.hiddenComponents().removeIf(PolymerComponent::isPolymerComponent);
        out.set(DataComponents.TOOLTIP_DISPLAY, display);

        try {
            var tooltip = itemStack.getTooltipLines(new PacketTooltipContext(context, lookup), null, tooltipContext);
            if (!tooltip.isEmpty()) {
                tooltip.removeFirst();

                if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
                    polymerItem.modifyClientTooltip(tooltip, itemStack, context);
                }
                if (!tooltip.isEmpty()) {
                    var lore = new ArrayList<Component>();
                    for (Component t : tooltip) {
                        lore.add(Component.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE));
                    }
                    out.set(DataComponents.LORE, new ItemLore(lore));
                }
            } else {
                out.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
            }
        } catch (Throwable e) {
            if (PolymerImpl.LOG_MORE_ERRORS) {
                PolymerImpl.LOGGER.error("Failed to get tooltip of " + itemStack, e);
            }
        }
        return PolymerItemUtils.ITEM_MODIFICATION_EVENT.invoker().modifyItem(itemStack, out, context);
    }
    public static final PacketContextProvider NULL_CONTEXT_PROVIDER = new PacketContextProvider() {
        @Override
        public PacketContext getPacketContext() {
            return null;
        }
    };
    private static final DataComponentType<?>[] COMPONENTS_TO_COPY = {
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.TRIM,
            DataComponents.TOOL,
            DataComponents.MAX_STACK_SIZE,
            DataComponents.MAP_ID,
            DataComponents.MAP_COLOR,
            DataComponents.MAP_DECORATIONS,
            DataComponents.MAP_POST_PROCESSING,
            DataComponents.FOOD,
            DataComponents.DAMAGE_RESISTANT,
            DataComponents.FIREWORKS,
            DataComponents.FIREWORK_EXPLOSION,
            DataComponents.DAMAGE,
            DataComponents.MAX_DAMAGE,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.BANNER_PATTERNS,
            DataComponents.BASE_COLOR,
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON,
            DataComponents.REPAIR_COST,
            DataComponents.BUNDLE_CONTENTS,
            DataComponents.TOOLTIP_STYLE,
            DataComponents.RARITY,
            DataComponents.LODESTONE_TRACKER,
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.POTION_CONTENTS,
            DataComponents.CUSTOM_NAME,
            DataComponents.JUKEBOX_PLAYABLE,
            DataComponents.WRITABLE_BOOK_CONTENT,
            DataComponents.WRITTEN_BOOK_CONTENT,
            DataComponents.CONTAINER,
            DataComponents.ENCHANTABLE,
            DataComponents.USE_COOLDOWN,
            DataComponents.CONSUMABLE,
            DataComponents.EQUIPPABLE,
            DataComponents.GLIDER,
            DataComponents.CUSTOM_MODEL_DATA,
            DataComponents.DYED_COLOR,
            DataComponents.REPAIRABLE,
            DataComponents.BLOCKS_ATTACKS,
            DataComponents.BREAK_SOUND,
            DataComponents.PROVIDES_BANNER_PATTERNS,
            DataComponents.PROVIDES_TRIM_MATERIAL,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.WEAPON,
            DataComponents.TOOLTIP_DISPLAY,
            DataComponents.KINETIC_WEAPON,
            DataComponents.PIERCING_WEAPON,
            DataComponents.ATTACK_RANGE,
            DataComponents.MINIMUM_ATTACK_CHARGE,
            DataComponents.SWING_ANIMATION,
            DataComponents.USE_EFFECTS
    };
    private static final ReferenceSet<DataComponentType<?>> FORCE_HIDE_TOOLTIP = ReferenceSet.of(
            DataComponents.UNBREAKABLE,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON
    );
    private static final ReferenceSet<DataComponentType<?>> IGNORE_TOOLTIP_HIDING = ReferenceSet.of(
            DataComponents.LORE
    );
    // end of polymer stuff

    public static AbstractContainerMenu createMenu(Container container, int id, Player player) {
        return createMenu(container, id, player, -1);
    }

    public static AbstractContainerMenu createMenu(Container container, int id, Player player, int lockSlot) {
        var est = de.tomalbrc.filament.behaviour.decoration.Container.estimateMenuType(container.getContainerSize());
        return new VirtualChestMenu(est, id, new PaginatedContainerGui(est, (ServerPlayer) player, false, container, lockSlot != -1), player, container, lockSlot);
    }

    public static void clickSound(ServerPlayer player) {
        player.connection.send(new ClientboundSoundPacket(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 0.5f, 0.1f, 0));
    }
}