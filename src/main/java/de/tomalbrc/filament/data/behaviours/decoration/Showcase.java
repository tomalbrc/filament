package de.tomalbrc.filament.data.behaviours.decoration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * For item showcase decoration
 */
public class Showcase {
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

    public enum ShowcaseType {
        item,
        block,
        dynamic // block when possible, item otherwise
    }
}