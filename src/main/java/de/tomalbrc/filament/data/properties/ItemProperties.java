package de.tomalbrc.filament.data.properties;

import de.tomalbrc.filament.behaviours.BehaviourConfigMap;
import de.tomalbrc.filament.behaviours.Behaviours;
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

    public Item.Properties toItemProperties(@Nullable BehaviourConfigMap behaviourConfigs) {
        Item.Properties props = new Item.Properties();
        props.stacksTo(stackSize);

        if (durability != Integer.MIN_VALUE)
            props.durability(durability);

        if (fireResistant)
            props.fireResistant();

        if (behaviourConfigs != null && behaviourConfigs.has(Behaviours.FOOD)) {
            var food = behaviourConfigs.get(Behaviours.FOOD);

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
