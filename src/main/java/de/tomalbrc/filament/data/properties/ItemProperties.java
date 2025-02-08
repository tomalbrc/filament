package de.tomalbrc.filament.data.properties;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// For shared properties that make sense for both, item, blocks *and* decoration
public class ItemProperties {
    public static final ItemProperties EMPTY = new ItemProperties();

    public int durability = Integer.MIN_VALUE;
    public int stackSize = 64;
    public List<String> lore;
    public boolean fireResistant;
    public boolean copyComponents;

    public void appendHoverText(List<Component> tooltip) {
        if (this.lore != null)
            this.lore.forEach(line -> tooltip.add(TextParserUtils.formatText(line)));
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
            builder.saturationModifier(food.saturation);
            builder.nutrition(food.hunger);

            props.food(builder.build());
        }

        return props;
    }
}
