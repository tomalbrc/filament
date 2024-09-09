package de.tomalbrc.filament.data.properties;

import de.tomalbrc.filament.behaviours.BehaviourConfigMap;
import de.tomalbrc.filament.behaviours.item.Food;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// For shared properties that make sense for both, item, blocks *and* decoration
public class ItemProperties {
    public int durability = Integer.MIN_VALUE;
    public int stackSize = 64;
    public List<String> lore;
    public boolean fireResistant;

    public void appendHoverText(List<Component> tooltip) {
        if (this.lore != null)
            this.lore.forEach(line -> tooltip.add(Component.literal(line)));
    }

    public Item.Properties toItemProperties() {
        return toItemProperties(null);
    }

    public Item.Properties toItemProperties(@Nullable BehaviourConfigMap behaviour) {
        Item.Properties props = new Item.Properties();
        props.stacksTo(stackSize);

        if (durability != Integer.MIN_VALUE)
            props.durability(durability);

        if (fireResistant)
            props.fireResistant();

        if (behaviour != null && behaviour.get(Constants.Behaviours.FOOD) != null) {
            Food.FoodConfig food = behaviour.get(Constants.Behaviours.FOOD);

            FoodProperties.Builder builder = new FoodProperties.Builder();
            if (food.canAlwaysEat) builder.alwaysEdible();
            if (food.fastfood) builder.fast();
            builder.saturationModifier(food.saturation);
            builder.nutrition(food.hunger);

            props.food(builder.build());
        }

        return props;
    }
}
