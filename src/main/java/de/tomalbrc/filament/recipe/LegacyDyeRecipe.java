package de.tomalbrc.filament.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class LegacyDyeRecipe implements CraftingRecipe {
    static TagKey<Item> DYEABLE = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dyeable"));

    @Override
    public boolean matches(CraftingInput input, @NonNull Level level) {
        ItemStack dyeable = ItemStack.EMPTY;
        List<ItemStack> dyes = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(DYEABLE)) {
                if (!dyeable.isEmpty()) return false;
                dyeable = stack;
            } else if (stack.is(ItemTags.DYES)) {
                dyes.add(stack);
            } else if (!stack.isEmpty()) {
                return false;
            }
        }

        return !dyeable.isEmpty() && !dyes.isEmpty();
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput input) {
        ItemStack dyeable = ItemStack.EMPTY;
        List<DyeColor> dyes = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(DYEABLE)) {
                if (!dyeable.isEmpty()) return ItemStack.EMPTY;
                dyeable = stack.copy();
            } else if (stack.has(DataComponents.DYE)) {
                dyes.add(stack.get(DataComponents.DYE));
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (dyeable.isEmpty() || dyes.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return DyedItemColor.applyDyes(dyeable, dyes);
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return Workstations.LEGACY_DYE_SERIALIZER;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }


    @Override
    public @NonNull PlacementInfo placementInfo() {
        var l = List.of(
                Ingredient.of(BuiltInRegistries.ITEM.get(DYEABLE).orElseThrow()),
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.DYES).orElseThrow())
        );
        return PlacementInfo.create(l);
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NonNull String group() {
        return "generic";
    }
}