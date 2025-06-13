package de.tomalbrc.filament.behaviour.decoration;

import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * For item showcase decoration
 */
public class Showcase implements DecorationBehaviour<Showcase.ShowcaseConfig> {
    private static final String SHOWCASE_KEY = "Showcase";
    private static final String ITEM = "Item";

    private final ShowcaseConfig config;

    Object2ObjectOpenHashMap<ShowcaseMeta, DisplayElement> showcases = new Object2ObjectOpenHashMap<>();

    public Showcase(ShowcaseConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ShowcaseConfig getConfig() {
        return this.config;
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

                    this.setShowcaseItemStack(decorationBlockEntity, showcase, itemStack.copyWithCount(1));
                    itemStack.shrink(1);
                    player.level().playSound(null, location.x(), location.y(), location.z(), SoundEvent.createVariableRangeEvent(showcase.addItemSound), SoundSource.NEUTRAL, 1.0f, 1.0f);
                } else if (showcaseStack != null && !showcaseStack.isEmpty()) {
                    changed = true;
                    player.setItemInHand(hand, showcaseStack);
                    this.setShowcaseItemStack(decorationBlockEntity, showcase, ItemStack.EMPTY);
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
    public void read(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        if (compoundTag.contains(SHOWCASE_KEY) && blockEntity.getOrCreateHolder() != null) {
            CompoundTag showcaseTag = compoundTag.getCompound(SHOWCASE_KEY).orElseThrow();
            FilamentDecorationHolder holder = blockEntity.getOrCreateHolder();
            if (holder == null)
                return;

            if (blockEntity.has(Behaviours.SHOWCASE)) {
                Showcase showcase = blockEntity.get(Behaviours.SHOWCASE);
                assert showcase != null;

                for (int i = 0; i < showcase.config.size(); i++) {
                    Showcase.ShowcaseMeta showcaseMeta = showcase.config.get(i);
                    String key = ITEM + i;
                    if (showcaseTag.contains(key)) {
                        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
                        setShowcaseItemStack(blockEntity, showcaseMeta, showcaseTag.read(key, ItemStack.CODEC, registryOps).orElseThrow());
                    }
                }
            }

        }
    }

    @Override
    public void write(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        if (blockEntity.getOrCreateHolder() != null) {
            CompoundTag showcaseTag = new CompoundTag();

            for (int i = 0; i < config.size(); i++) {
                Showcase.ShowcaseMeta showcase = config.get(i);
                if (showcase != null && !getShowcaseItemStack(showcase).isEmpty())
                    showcaseTag.put(ITEM + i, getShowcaseItemStack(showcase).save(provider));
            }

            compoundTag.put(SHOWCASE_KEY, showcaseTag);
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
        }
        else {
            double dist = Double.MAX_VALUE;
            Showcase.ShowcaseMeta nearest = null;
            for (Showcase.ShowcaseMeta showcase : config) {
                Vec3 q = decorationBlockEntity.getBlockPos().getCenter().add(new Vec3(this.showcaseTranslation(decorationBlockEntity, showcase)));
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
        return new Vector3f(showcase.offset).sub(0, 0.475f, 0).rotate(Axis.YN.rotation(Mth.DEG_TO_RAD * decorationBlockEntity.getVisualRotationYInDegrees()));
    }

    public void setShowcaseItemStack(DecorationBlockEntity decorationBlockEntity, Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
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
                blockDisplayElement.setBlockState(blockItem.getBlock().defaultBlockState());
            } else if (element instanceof ItemDisplayElement itemDisplayElement) {
                itemDisplayElement.setItem(itemStack);
            }
        }

        decorationBlockEntity.getOrCreateHolder().tick();
    }

    private BlockDisplayElement element(BlockItem blockItem) {
        BlockDisplayElement displayElement = new BlockDisplayElement();
        displayElement.setBlockState(blockItem.getBlock().defaultBlockState());
        return displayElement;
    }
    private ItemDisplayElement element(ItemStack itemStack) {
        ItemDisplayElement displayElement = new ItemDisplayElement();
        displayElement.setInvisible(true);
        displayElement.setItem(itemStack.copy());
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
            element.setScale(showcase.scale);
            element.setLeftRotation(showcase.rotation);
            Quaternionf rot = Axis.YN.rotationDegrees(decorationBlockEntity.getVisualRotationYInDegrees()+180).normalize();
            if (element instanceof BlockDisplayElement) {
                element.setTranslation(this.showcaseTranslation(decorationBlockEntity, showcase).add(new Vector3f(-.5f, -.5f, -.5f).rotate(rot).mul(showcase.scale)));
            } else {
                element.setTranslation(this.showcaseTranslation(decorationBlockEntity, showcase));
            }

            element.setRightRotation(rot);

            this.showcases.put(showcase, element);
        } else {
            Filament.LOGGER.error("In valid showcase type for " + itemStack.getItem().getDescriptionId());
        }

        return element;
    }

    public ItemStack getShowcaseItemStack(Showcase.ShowcaseMeta showcase) {
        DisplayElement element = this.showcases.get(showcase);
        if (element instanceof ItemDisplayElement itemDisplayElement) {
            return itemDisplayElement.getItem().copy();
        } else if (element instanceof BlockDisplayElement itemDisplayElement) {
            return itemDisplayElement.getBlockState().getBlock().asItem().getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }

    public boolean canUseShowcaseItem(Showcase.ShowcaseMeta showcase, ItemStack item) {
        boolean hasFilterItems = showcase.filterItems != null && !showcase.filterItems.isEmpty();
        boolean hasFilterTags = showcase.filterTags != null && !showcase.filterTags.isEmpty();

        if (hasFilterTags) {
            for (var filterTag: showcase.filterTags) {
                TagKey<Item> key = TagKey.create(Registries.ITEM, filterTag);
                if (item.is(key)) {
                    return true;
                }
            }
        }

        if (hasFilterItems) {
            for (var filterTag: showcase.filterItems) {
                if (item.is(filterTag)) {
                    return true;
                }
            }
        }

        return !(hasFilterItems || hasFilterTags);
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
    }

    public enum ShowcaseType {
        item,
        block,
        dynamic // block when possible, item otherwise
    }

    public static class ShowcaseConfig extends ObjectArrayList<ShowcaseMeta> { }
}