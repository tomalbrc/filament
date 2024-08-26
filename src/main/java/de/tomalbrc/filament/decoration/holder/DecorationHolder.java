package de.tomalbrc.filament.decoration.holder;

import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.behaviours.decoration.Seat;
import de.tomalbrc.filament.data.behaviours.decoration.Showcase;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.registry.filament.EntityRegistry;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DecorationHolder extends ElementHolder {

    DecorationBlockEntity parent;

    // TODO: for block showcases, we need a reference to the original ItemStack the player put in there..!
    Object2ObjectOpenHashMap<Showcase.ShowcaseMeta, DisplayElement> showcases = new Object2ObjectOpenHashMap<>();

    Showcase showcaseDataList;
    Seat seatDataList;

    public DecorationHolder() {
        super();
    }

    public void setBlockEntity(DecorationBlockEntity blockEntity) {
        this.parent = blockEntity;

        if (blockEntity.getDecorationData().hasBlocks()) {
            this.addElement(Util.decorationItemDisplay(this.parent));
        } else if (blockEntity.getDecorationData().size() != null) {
            this.addElement(Util.decorationItemDisplay(this.parent));
            this.addElement(Util.decorationInteraction(this.parent));
        } else {
            // TODO: Why do virtual Item frames show item names?!
            //ItemFrameElement itemFrameElement = new ItemFrameElement(this.parent);
            //this.addElement(itemFrameElement);

            // Just using display+interaction again
            this.addElement(Util.decorationItemDisplay(this.parent));
            this.addElement(Util.decorationInteraction(this.parent));
        }
    }

    public Showcase.ShowcaseMeta getClosestShowcase(Vec3 location) {
        if (this.showcaseDataList.size() == 1) {
            return this.showcaseDataList.get(0);
        }
        else {
            double dist = Double.MAX_VALUE;
            Showcase.ShowcaseMeta nearest = null;
            for (Showcase.ShowcaseMeta showcase : this.showcaseDataList) {
                Vec3 q = this.parent.getBlockPos().getCenter().add(new Vec3(this.showcaseTranslation(showcase)));
                double distance = q.distanceTo(location);

                if (distance < dist) {
                    dist = distance;
                    nearest = showcase;
                }
            }

            return nearest;
        }
    }

    public Seat.SeatMeta getClosestSeat(Vec3 location) {
        if (this.seatDataList.size() == 1) {
            return this.seatDataList.get(0);
        }
        else {
            double dist = Double.MAX_VALUE;
            Seat.SeatMeta nearest = null;

            for (Seat.SeatMeta seat : this.seatDataList) {
                Vec3 q = this.parent.getBlockPos().getCenter().add(seatTranslation(seat));
                double distance = q.distanceTo(location);

                if (!this.hasSeatedPlayer(seat) && distance < dist) {
                    dist = distance;
                    nearest = seat;
                }
            }

            return nearest;
        }
    }

    public Vec3 seatTranslation(Seat.SeatMeta seat) {
        Vec3 v3 = new Vec3(seat.offset).subtract(0, 0.3, 0).yRot((float) Math.toRadians(this.parent.getVisualRotationYInDegrees()+180));
        return new Vec3(-v3.x, v3.y, v3.z);
    }

    private Vector3f showcaseTranslation(Showcase.ShowcaseMeta showcase) {
        return new Vector3f(showcase.offset).sub(0, 0.475f, 0).rotate(Axis.YN.rotation(Mth.DEG_TO_RAD * this.parent.getVisualRotationYInDegrees()));
    }
    private BlockDisplayElement element(BlockItem blockItem) {
        BlockDisplayElement displayElement = new BlockDisplayElement();
        displayElement.setBlockState(blockItem.getBlock().defaultBlockState());
        return displayElement;
    }
    private ItemDisplayElement element(ItemStack itemStack) {
        ItemDisplayElement displayElement = new ItemDisplayElement();
        displayElement.setItem(itemStack.copy());
        return displayElement;
    }
    private DisplayElement createShowcase(Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
        DisplayElement element = null;

        switch (showcase.type) {
            case item -> element = this.element(itemStack);
            case block -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem) {
                    element = this.element(blockItem);
                }
            }
            case dynamic -> {
                if (itemStack.getItem().asItem() instanceof BlockItem blockItem) {
                    element = this.element(blockItem);
                } else {
                    element = this.element(itemStack);
                }
            }
        }

        if (element != null) {
            element.setScale(showcase.scale);
            element.setLeftRotation(showcase.rotation);
            Quaternionf rot = Axis.YN.rotationDegrees(this.parent.getVisualRotationYInDegrees()+180).normalize();
            if (element instanceof BlockDisplayElement) {
                element.setTranslation(this.showcaseTranslation(showcase).add(new Vector3f(-.5f, -.5f, -.5f).rotate(rot).mul(showcase.scale)));
            } else {
                element.setTranslation(this.showcaseTranslation(showcase));
            }

            element.setRightRotation(rot);

            this.showcases.put(showcase, element);
        } else {
            Filament.LOGGER.error("In valid showcase type for " + itemStack.getItem().getDescriptionId());
        }

        return element;
    }

    public void setShowcaseData(Showcase showcaseData) {
        this.showcaseDataList = showcaseData;
    }

    public Showcase getShowcaseData() {
        return this.showcaseDataList;
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

    public void setShowcaseItemStack(Showcase.ShowcaseMeta showcase, ItemStack itemStack) {
        boolean isBlockItem = itemStack.getItem() instanceof BlockItem;
        boolean hasElement = this.showcases.containsKey(showcase);

        DisplayElement element = this.showcases.get(showcase);
        DisplayElement newElement;

        boolean dynNeedsUpdate = showcase.type == Showcase.ShowcaseType.dynamic && hasElement && !(element instanceof BlockDisplayElement && isBlockItem);

        if (!this.showcases.containsKey(showcase) || dynNeedsUpdate) {
            if (hasElement) { // update dynamic display, remove old
                this.removeElement(element);
                this.showcases.remove(showcase);
            }

            newElement = this.createShowcase(showcase, itemStack);
            this.addElement(newElement);
        } else {
            if (element instanceof BlockDisplayElement blockDisplayElement && itemStack.getItem() instanceof BlockItem blockItem) {
                blockDisplayElement.setBlockState(blockItem.getBlock().defaultBlockState());
            } else if (element instanceof ItemDisplayElement itemDisplayElement) {
                itemDisplayElement.setItem(itemStack);
            }
        }

        this.tick();
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

    public void setSeatData(Seat seatData) {
        this.seatDataList = seatData;
    }

    public void seatPlayer(Seat.SeatMeta seat, ServerPlayer player) {
        SeatEntity seatEntity = EntityRegistry.SEAT_ENTITY.create(player.level());
        seatEntity.setPos(this.seatTranslation(seat).add(this.getPos()));
        player.level().addFreshEntity(seatEntity);
        player.startRiding(seatEntity);
        seatEntity.setYRot((this.parent.getVisualRotationYInDegrees()-180));
    }

    public boolean hasSeatedPlayer(Seat.SeatMeta seat) {
        return !this.getAttachment().getWorld().getEntitiesOfClass(SeatEntity.class, AABB.ofSize(seatTranslation(seat).add(this.getPos()), 0.2, 0.2, 0.2), x -> true).isEmpty();
    }

    public boolean isSeat() {
        return this.seatDataList != null && !this.seatDataList.isEmpty();
    }

    public boolean isShowcase() {
        return this.showcaseDataList != null && !this.showcaseDataList.isEmpty();
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    public Vec3 getPos() {
        return this.getAttachment() != null ? this.getAttachment().getPos() : null;
    }
}
