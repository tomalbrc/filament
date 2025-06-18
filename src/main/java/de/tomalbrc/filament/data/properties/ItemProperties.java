package de.tomalbrc.filament.data.properties;

import de.tomalbrc.filament.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Consumer;

// For shared properties that make sense for items, blocks *and* decorations
public class ItemProperties {
    public int durability = Integer.MIN_VALUE;
    public int stackSize = 64;
    public List<String> lore;
    public boolean fireResistant;
    public boolean copyComponents;

    public void appendHoverText(Consumer<Component> tooltip) {
        if (this.lore != null)
            this.lore.forEach(line -> tooltip.accept(TextUtil.formatText(line)));
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
