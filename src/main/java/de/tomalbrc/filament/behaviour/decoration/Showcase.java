package de.tomalbrc.filament.behaviour.decoration;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.TextUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.List;

/**
 * For item showcase decoration
 */
public class Showcase implements BlockBehaviour<Showcase.Config>, DecorationBehaviour<Showcase.Config>, ContainerLike {
    private static final String SHOWCASE_KEY = "Showcase";
    private static final String ITEM = "Item";

    private final Config config;

    private final Object2ObjectOpenHashMap<ShowcaseMeta, DisplayElement> showcases = new Object2ObjectOpenHashMap<>();

    private FilamentContainer container;

    public Showcase(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Showcase.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        this.container = new FilamentContainer(blockEntity, config.elements.size(), false) {
            @Override
            public int getMaxStackSize(int slot) {
                return config.elements.get(slot).maxStackSize;
            }
        };

        this.container.addListener(x -> {
            for (int i = 0; i < x.getContainerSize(); i++) {
                var stack = x.getItem(i);
                setShowcaseItemStack(blockEntity, config.elements.get(i), stack);
            }
        });
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!player.isSecondaryUseActive() && decorationBlockEntity.getOrCreateHolder() != null) {
            if (config.useMenu) {
                Component containerName = customName() != null && showCustomName() ? customName() : TextUtil.formatText(config.name);
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> Util.createMenu(container, id, inventory, p), containerName));
                return InteractionResult.SUCCESS;
            }

            Showcase.ShowcaseMeta showcase = getClosestShowcase(decorationBlockEntity, location);
            ItemStack itemStack = player.getItemInHand(hand);
            ItemStack showcaseStack = this.getShowcaseItemStack(showcase);

            if (this.canUseShowcaseItem(showcase, itemStack)) {
                boolean changed = false;
                if (!player.getItemInHand(hand).isEmpty()) {
                    changed = true;
                    if (showcaseStack != null && !showcaseStack.isEmpty()) {
                        Util.spawnAtLocation(decorationBlockEntity.getLevel(), location, showcaseStack);
                    }

                    var count = container.getMaxStackSize(0);
                    this.container.setItem(config.elements.indexOf(showcase), itemStack.copyWithCount(count));
                    itemStack.shrink(count);
                    player.level().playSound(null, location.x(), location.y(), location.z(), SoundEvent.createVariableRangeEvent(showcase.addItemSound), SoundSource.NEUTRAL, 1.0f, 1.0f);
                } else if (showcaseStack != null && !showcaseStack.isEmpty()) {
                    changed = true;
                    player.setItemInHand(hand, showcaseStack);
                    this.container.setItem(config.elements.indexOf(showcase), ItemStack.EMPTY);
                    player.level().playSound(null, location.x(), location.y(), location.z(), SoundEvent.createVariableRangeEvent(showcase.removeItemSound), SoundSource.NEUTRAL, 1.0f, 1.0f);
                }

                if (changed) {
                    decorationBlockEntity.setChanged();
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void read(ValueInput output, DecorationBlockEntity blockEntity) {
        var showcaseInput = output.child(SHOWCASE_KEY);
        if (showcaseInput.isPresent() && blockEntity.getOrCreateHolder() != null) {
            ValueInput showcaseTag = showcaseInput.orElseThrow();

            for (int i = 0; i < this.config.elements.size(); i++) {
                String key = ITEM + i;
                if (showcaseTag.child(key).isPresent()) {
                    container.items.set(i, showcaseTag.read(key, ItemStack.CODEC).orElseThrow());
                }
            }
            container.setChanged();
        }
    }

    @Override
    public void write(ValueOutput output, DecorationBlockEntity blockEntity) {
        if (blockEntity.getOrCreateHolder() != null) {
            ValueOutput showcaseTag = output.child(SHOWCASE_KEY);

            for (int i = 0; i < config.elements.size(); i++) {
                Showcase.ShowcaseMeta showcase = config.elements.get(i);
                if (showcase != null && !getShowcaseItemStack(showcase).isEmpty())
                    showcaseTag.store(ITEM + i, ItemStack.CODEC, getShowcaseItemStack(showcase));
            }
        }
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        container.setValid(false);

        if (!config.canPickup) {
            Containers.dropContents(decorationBlockEntity.getLevel(), decorationBlockEntity.getBlockPos(), container);
        }
    }

    public Showcase.ShowcaseMeta getClosestShowcase(DecorationBlockEntity decorationBlockEntity, Vec3 location) {
        if (config.elements.size() == 1) {
            return config.elements.getFirst();
        } else {
            double dist = Double.MAX_VALUE;
            Showcase.ShowcaseMeta nearest = null;
            for (Showcase.ShowcaseMeta showcase : config.elements) {
                Vec3 q = decorationBlockEntity.getBlockPos().getCenter().add(new Vec3(this.showcaseTranslation(showcase).rotateY((-decorationBlockEntity.getVisualRotationYInDegrees() + 180) * Mth.DEG_TO_RAD)));
                double distance = q.distanceTo(location);

                if (distance < dist) {
                    dist = distance;
                    nearest = showcase;
                }
            }

            return nearest;
        }
    }

    private Vector3f showcaseTranslation(Showcase.ShowcaseMeta showcase) {
        return new Vector3f(showcase.offset).sub(0, 0.475f, 0).rotateY(Mth.PI);
    }

    public void setShowcaseItemStack(DecorationBlockEntity decorationBlockEntity, Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
        FilamentDecorationHolder holder = decorationBlockEntity.getOrCreateHolder();
        if (holder == null)
            return;

        boolean isBlockItem = itemStack.getItem() instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof DecorationBlock);

        DisplayElement element = this.showcases.get(showcase);

        DisplayElement newElement;

        boolean dynNeedsUpdate = showcase.type == Showcase.ShowcaseType.dynamic && element != null && !(element instanceof BlockDisplayElement && isBlockItem);

        if (element == null || dynNeedsUpdate) {
            if (element != null) { // update dynamic display, remove old
                decorationBlockEntity.getOrCreateHolder().removeElement(element);
                this.showcases.remove(showcase);
            }

            newElement = this.createShowcase(decorationBlockEntity, showcase, itemStack);
            decorationBlockEntity.getOrCreateHolder().addElement(newElement);
        } else {
            if (element instanceof BlockDisplayElement blockDisplayElement && itemStack.getItem() instanceof BlockItem blockItem) {
                blockDisplayElement.getDataTracker().set(DisplayTrackedData.Block.BLOCK_STATE, blockItem.getBlock().defaultBlockState(), true);
            } else if (element instanceof ItemDisplayElement itemDisplayElement) {
                itemDisplayElement.setItem(itemStack);
            }

            this.showcases.put(showcase, element);
        }

        decorationBlockEntity.getOrCreateHolder().tick();
    }

    private BlockDisplayElement element(BlockItem blockItem) {
        BlockDisplayElement displayElement = new BlockDisplayElement();
        displayElement.setBlockState(blockItem.getBlock().defaultBlockState());
        return displayElement;
    }

    private DisplayElement element(ShowcaseMeta showcase, ItemStack itemStack) {
        ItemDisplayElement displayElement = new ItemDisplayElement(itemStack.copy());
        displayElement.setItemDisplayContext(showcase.display);
        displayElement.setInvisible(true);
        return displayElement;
    }

    private DisplayElement createShowcase(DecorationBlockEntity decorationBlockEntity, Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
        DisplayElement element = null;

        switch (showcase.type) {
            case item -> element = this.element(showcase, itemStack);
            case block -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem && !(blockItem instanceof DecorationItem)) {
                    element = this.element(blockItem);
                }
            }
            case dynamic -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem && !(blockItem instanceof DecorationItem)) {
                    element = this.element(blockItem);
                } else {
                    element = this.element(showcase, itemStack);
                }
            }
        }

        if (element != null) {
            transform(decorationBlockEntity, element, showcase);
            this.showcases.put(showcase, element);
        } else {
            Filament.LOGGER.error("In valid showcase type for {}", itemStack.getItem().getDescriptionId());
        }

        return element;
    }

    private void transform(DecorationBlockEntity decorationBlockEntity, DisplayElement element, ShowcaseMeta showcase) {
        if (element != null) {
            element.setScale(showcase.scale);
            element.setLeftRotation(showcase.rotation);
            element.setYaw(decorationBlockEntity.getVisualRotationYInDegrees() + 180);
            if (element instanceof BlockDisplayElement) {
                element.setTranslation(this.showcaseTranslation(showcase).add(new Vector3f(-.5f, -.5f, -.5f).mul(showcase.scale)));
            } else {
                element.setTranslation(this.showcaseTranslation(showcase));
            }
        }
    }

    public ItemStack getShowcaseItemStack(Showcase.ShowcaseMeta showcase) {
        var i = config.elements.indexOf(showcase);
        return container.getItem(i);
    }

    public boolean canUseShowcaseItem(Showcase.ShowcaseMeta showcase, ItemStack item) {
        boolean hasFilterItems = showcase.filterItems != null && !showcase.filterItems.isEmpty();
        boolean hasFilterTags = showcase.filterTags != null && !showcase.filterTags.isEmpty();

        if (hasFilterTags) {
            for (var filterTag : showcase.filterTags) {
                TagKey<Item> key = TagKey.create(Registries.ITEM, filterTag);
                if (item.is(key)) {
                    return true;
                }
            }
        }

        if (hasFilterItems) {
            for (var filterTag : showcase.filterItems) {
                if (item.is(filterTag)) {
                    return true;
                }
            }
        }

        return !(hasFilterItems || hasFilterTags);
    }

    @Override
    public void modifyDrop(DecorationBlockEntity decorationBlockEntity, ItemStack itemStack) {
        if (config.canPickup) {
            itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
        }
    }

    @Override
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return itemStack;
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentGetter dataComponentGetter) {
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(container.items);
        container.setChanged();
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.items));
    }

    @Override
    public Component customName() {
        return container.getBlockEntity().components().get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public @Nullable Container container() {
        return container;
    }

    @Override
    public boolean showCustomName() {
        return config.showCustomName;
    }

    @Override
    public boolean hopperDropperSupport() {
        return config.hopperDropperSupport;
    }

    public static class ShowcaseMeta {
        /**
         * Offset for positioning the showcased item
         */
        public Vector3f offset = new Vector3f();

        /**
         * Scale of the showcased item
         */
        public Vector3f scale = new Vector3f(1);

        /**
         * Rotation of the showcased item
         */
        public Quaternionf rotation = new Quaternionf();

        /**
         * Type to display, block for blocks (block display), item for items (item display), dynamic uses blocks if possible, otherwise item (block/item display)
         */
        public ShowcaseType type = ShowcaseType.item;

        /**
         * Items to allow
         */
        public List<Item> filterItems;

        /**
         * Items with given item tags to allow
         */
        public List<ResourceLocation> filterTags;

        public ResourceLocation addItemSound = SoundEvents.ITEM_FRAME_ADD_ITEM.location();

        public ResourceLocation removeItemSound = SoundEvents.ITEM_FRAME_REMOVE_ITEM.location();

        public int maxStackSize = 1;

        public ItemDisplayContext display = ItemDisplayContext.FIXED;
    }

    public enum ShowcaseType {
        item,
        block,
        dynamic // block when possible, item otherwise
    }

    public static class Config {
        public boolean hopperDropperSupport = true;
        public boolean useMenu = false;
        public String name = "Showcase";
        public boolean showCustomName = true;
        public boolean canPickup = false;
        public List<ShowcaseMeta> elements;

        public static class ConfigDeserializer implements JsonDeserializer<Config> {
            @Override
            public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
                Config config = new Config();
                if (json.isJsonArray()) {
                    config.elements = new ObjectArrayList<>();
                    for (JsonElement elem : json.getAsJsonArray()) {
                        ShowcaseMeta meta = ctx.deserialize(elem, ShowcaseMeta.class);
                        config.elements.add(meta);
                    }
                    return config;
                }

                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();

                    if (obj.has("hopper_dropper_support"))
                        config.hopperDropperSupport = obj.get("hopper_dropper_support").getAsBoolean();
                    if (obj.has("use_menu"))
                        config.useMenu = obj.get("use_menu").getAsBoolean();
                    if (obj.has("name"))
                        config.name = obj.get("name").getAsString();
                    if (obj.has("show_custom_name"))
                        config.showCustomName = obj.get("show_custom_name").getAsBoolean();

                    if (obj.has("elements") && obj.get("elements").isJsonArray()) {
                        config.elements = new ObjectArrayList<>();
                        for (JsonElement elem : obj.getAsJsonArray("elements")) {
                            ShowcaseMeta meta = ctx.deserialize(elem, ShowcaseMeta.class);
                            config.elements.add(meta);
                        }
                    }
                    return config;
                }

                throw new JsonParseException("Invalid config format: must be object or array");
            }
        }
    }
}