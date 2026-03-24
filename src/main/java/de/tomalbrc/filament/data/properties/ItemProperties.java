package de.tomalbrc.filament.data.properties;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Consumer;

// For shared properties that make sense for items, blocks *and* decorations
public class ItemProperties {
    public Integer durability = null;
    public Integer stackSize = 64;
    public List<Component> lore;
    public Boolean fireResistant;
    public Boolean copyComponents;
    public Boolean copyTags;

    public void appendHoverText(Consumer<Component> tooltip) {
        if (this.lore != null)
            this.lore.forEach(tooltip);
    }

    public Item.Properties toItemProperties() {
        Item.Properties props = new Item.Properties();
        if (stackSize != null) props.stacksTo(stackSize);

        if (durability != null)
            props.durability(durability);

        if (fireResistant == Boolean.TRUE)
            props.fireResistant();

        return props;
    }
}
