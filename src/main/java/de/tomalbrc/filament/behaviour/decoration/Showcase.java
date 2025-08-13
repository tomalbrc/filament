package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
        this.container = new FilamentContainer(blockEntity, config.size(), false) {
            @Override
            public int getMaxStackSize(int slot) {
                return config.get(slot).maxStackSize;
            }
        };

        this.container.addListener(x -> {
            for (int i = 0; i < x.getContainerSize(); i++) {
                var stack = x.getItem(i);
                setShowcaseItemStack(blockEntity, config.get(i), stack);
            }
        });
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!player.isSecondaryUseActive() && decorationBlockEntity.getOrCreateHolder() != null) {
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
                    this.container.setItem(config.indexOf(showcase), itemStack.copyWithCount(count));
                    itemStack.shrink(count);
                    player.level().playSound(null, location.x(), location.y(), location.z(), SoundEvent.createVariableRangeEvent(showcase.addItemSound), SoundSource.NEUTRAL, 1.0f, 1.0f);
                } else if (showcaseStack != null && !showcaseStack.isEmpty()) {
                    changed = true;
                    player.setItemInHand(hand, showcaseStack);
                    this.container.setItem(config.indexOf(showcase), ItemStack.EMPTY);
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

            for (int i = 0; i < this.config.size(); i++) {
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

            for (int i = 0; i < config.size(); i++) {
                Showcase.ShowcaseMeta showcase = config.get(i);
                if (showcase != null && !getShowcaseItemStack(showcase).isEmpty())
                    showcaseTag.store(ITEM + i, ItemStack.CODEC, getShowcaseItemStack(showcase));
            }
        }
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        if (decorationBlockEntity.getOrCreateHolder() != null) {
            config.forEach(showcase -> {
                ItemStack itemStack = getShowcaseItemStack(showcase);
                if (itemStack != null && !itemStack.isEmpty()) {
                    Util.spawnAtLocation(decorationBlockEntity.getLevel(), decorationBlockEntity.getBlockPos().getCenter(), itemStack.copyAndClear());
                }
            });
        }
    }

    public Showcase.ShowcaseMeta getClosestShowcase(DecorationBlockEntity decorationBlockEntity, Vec3 location) {
        if (config.size() == 1) {
            return config.getFirst();
        } else {
            double dist = Double.MAX_VALUE;
            Showcase.ShowcaseMeta nearest = null;
            for (Showcase.ShowcaseMeta showcase : config) {
                Vec3 q = decorationBlockEntity.getBlockPos().getCenter().add(new Vec3(this.showcaseTranslation(decorationBlockEntity, showcase).rotateY((-decorationBlockEntity.getVisualRotationYInDegrees() + 180) * Mth.DEG_TO_RAD)));
                double distance = q.distanceTo(location);

                if (distance < dist) {
                    dist = distance;
                    nearest = showcase;
                }
            }

            return nearest;
        }
    }

    private Vector3f showcaseTranslation(DecorationBlockEntity decorationBlockEntity, Showcase.ShowcaseMeta showcase) {
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
            } else if (element instanceof ShowcaseItemElement itemDisplayElement) {
                itemDisplayElement.setItemReal(itemStack);
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

    private ItemDisplayElement element(ItemStack itemStack) {
        ItemDisplayElement displayElement = new ShowcaseItemElement(itemStack.copy());
        displayElement.setInvisible(true);
        return displayElement;
    }

    private DisplayElement createShowcase(DecorationBlockEntity decorationBlockEntity, Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
        DisplayElement element = null;

        switch (showcase.type) {
            case item -> element = this.element(itemStack);
            case block -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem && !(blockItem instanceof DecorationItem)) {
                    element = this.element(blockItem);
                }
            }
            case dynamic -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem && !(blockItem instanceof DecorationItem)) {
                    element = this.element(blockItem);
                } else {
                    element = this.element(itemStack);
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
                element.setTranslation(this.showcaseTranslation(decorationBlockEntity, showcase).add(new Vector3f(-.5f, -.5f, -.5f).mul(showcase.scale)));
            } else {
                element.setTranslation(this.showcaseTranslation(decorationBlockEntity, showcase));
            }
        }
    }

    public ItemStack getShowcaseItemStack(Showcase.ShowcaseMeta showcase) {
        var i = config.indexOf(showcase);
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
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return DecorationBehaviour.super.getCloneItemStack(itemStack, levelReader, blockPos, blockState, includeData);
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentGetter dataComponentGetter) {
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(container.items);
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.items));
    }

    @Override
    public Component customName() {
        return null;
    }

    @Override
    public @Nullable FilamentContainer container() {
        return container;
    }

    @Override
    public boolean showCustomName() {
        return false;
    }

    @Override
    public boolean hopperDropperSupport() {
        for (ShowcaseMeta meta : config) {
            if (meta.hopperDropperSupport)
                return true;
        }
        return false;
    }

    @Override
    public boolean canPickup() {
        return false;
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

        public boolean hopperDropperSupport = true;

        public int maxStackSize = 1;
    }

    public enum ShowcaseType {
        item,
        block,
        dynamic // block when possible, item otherwise
    }

    public static class Config extends ObjectArrayList<ShowcaseMeta> {
    }

    private static class ShowcaseItemElement extends ItemDisplayElement {
        public ShowcaseItemElement(ItemStack itemStack) {
            setItemReal(itemStack);
        }

        @Override
        public void setItem(ItemStack stack) {

        }

        public void setItemReal(ItemStack stack) {
            this.dataTracker.set(DisplayTrackedData.Item.ITEM, stack, true);
        }
    }
}