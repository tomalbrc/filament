package de.tomalbrc.filament.data.properties;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Consumer;

// For shared properties that make sense for items, blocks *and* decorations
public class ItemProperties {
    public int durability = Integer.MIN_VALUE;
    public int stackSize = 64;
    public List<Component> lore;
    public boolean fireResistant;
    public boolean copyComponents;

    public void appendHoverText(Consumer<Component> tooltip) {
        if (this.lore != null)
            this.lore.forEach(tooltip);
    }

    public Item.Properties toItemProperties() {
        Item.Properties props = new Item.Properties();
        props.stacksTo(stackSize);

        if (durability != Integer.MIN_VALUE)
            props.durability(durability);

        if (fireResistant)
            props.fireResistant();

        return props;
    }
}
