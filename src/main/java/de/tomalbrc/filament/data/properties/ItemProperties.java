package de.tomalbrc.filament.data.properties;

import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import de.tomalbrc.filament.data.behaviours.item.ItemBehaviourList;

import java.util.List;

// For shared properties that make sense for both, item *and* decoration
public class ItemProperties {
    public int durability = Integer.MIN_VALUE;
    public int stackSize = 64;
    public List<String> lore;
    public boolean fireResistant;

    // for blocks & decoration
    public int lightEmission = Integer.MIN_VALUE;

    public void appendHoverText(List<Component> tooltip) {
        if (lore != null)
            lore.forEach(line -> tooltip.add(Component.literal(line)));
    }

    public Item.Properties toItemProperties() {
        return toItemProperties(null, null);
    }

    public Item.Properties toItemProperties(@Nullable Item vanillaItem, @Nullable ItemBehaviourList behaviour) {
        Item.Properties props = new Item.Properties();

        props.stacksTo(stackSize);

        if (durability != Integer.MIN_VALUE)
            props.durability(durability);

        if (fireResistant)
            props.fireResistant();

        if (behaviour != null && behaviour.food != null) {
            FoodProperties.Builder builder = new FoodProperties.Builder();

            if (behaviour.food.meat) builder.meat();
            if (vanillaItem != null) {
                FoodProperties vanillaFood = vanillaItem.getFoodProperties();
                if (vanillaFood != null) {
                    if (vanillaFood.isFastFood()) builder.fast();
                    if (vanillaFood.canAlwaysEat()) builder.alwaysEat();
                }
            }

            builder.saturationMod(behaviour.food.saturation);
            builder.nutrition(behaviour.food.hunger);

            props.food(builder.build());
        }

        return props;
    }

    public boolean isLightSource() {
        return this.lightEmission != Integer.MIN_VALUE;
    }
}
