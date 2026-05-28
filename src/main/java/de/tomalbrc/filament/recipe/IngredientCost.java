package de.tomalbrc.filament.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.UnaryOperator;

public record IngredientCost(Holder<Item> item, int count, DataComponentExactPredicate components) {
    public static final Codec<IngredientCost> CODEC = RecordCodecBuilder.create(i -> i.group((Item.CODEC.fieldOf("id")).forGetter(IngredientCost::item), (ExtraCodecs.POSITIVE_INT.fieldOf("count")).orElse(1).forGetter(IngredientCost::count), DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(IngredientCost::components)).apply(i, IngredientCost::new));

    public IngredientCost(ItemLike item) {
        this(item, 1);
    }

    public IngredientCost(ItemLike item, int count) {
        this(item.asItem().builtInRegistryHolder(), count, DataComponentExactPredicate.EMPTY);
    }

    public IngredientCost withComponents(UnaryOperator<DataComponentExactPredicate.Builder> components) {
        return new IngredientCost(this.item, this.count, components.apply(DataComponentExactPredicate.builder()).build());
    }

    private static ItemStack createStack(Holder<Item> item, int count, DataComponentExactPredicate components) {
        return new ItemStack(item, count, components.asPatch());
    }

    public boolean test(ItemStack itemStack) {
        return itemStack.is(this.item) && this.components.test(itemStack);
    }
}
