package de.tomalbrc.filament.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Stream;

public record StationRecipe(
        Identifier stationId,
        Map<Integer, Either<Ingredient, IngredientCost>> ingredients, // positional / shaped
        Map<Integer, ItemStackTemplate> results,
        int processingTime,
        Map<String, List<Either<Ingredient, IngredientCost>>> groupIngredients, // group mapped to ingredient list for shapeless
        List<String> pattern,
        Map<String, Either<Ingredient, IngredientCost>> key
) implements Recipe<StationRecipeInput> {

    static Codec<Either<Ingredient, IngredientCost>> EITHER_CODEC = Codec.either(Ingredient.CODEC, IngredientCost.CODEC);

    public boolean isShapeless() {
        return !groupIngredients.isEmpty() && pattern == null;
    }

    public boolean isShaped() {
        return pattern != null && key != null;
    }

    @Override
    public boolean matches(@NonNull StationRecipeInput inv, @NonNull Level level) {
        StationDef def = Workstations.get(stationId);
        if (def == null) return false;

        if (isShaped()) {
            if (def.grid().isEmpty()) return false;
            int gridRows = def.grid().get().rows();
            int gridCols = def.grid().get().columns();
            int patRows = pattern.size();
            int patCols = pattern.getFirst().length();
            if (patRows > gridRows || patCols > gridCols) return false;

            for (int dy = 0; dy <= gridRows - patRows; dy++) {
                for (int dx = 0; dx <= gridCols - patCols; dx++) {
                    boolean match = true;
                    Set<Integer> coveredSlots = new HashSet<>();

                    outer:
                    for (int r = 0; r < patRows; r++) {
                        for (int c = 0; c < patCols; c++) {
                            char ch = pattern.get(r).charAt(c);
                            if (ch == ' ') continue;
                            var ing = key.get(ch);
                            if (ing == null) {
                                match = false;
                                break outer;
                            }
                            Optional<Integer> slotIdx = def.getSlotIndexAt(dy + r, dx + c);
                            if (slotIdx.isEmpty()) {
                                match = false;
                                break outer;
                            }
                            int slot = slotIdx.get();
                            coveredSlots.add(slot);
                            if (matches(ing, inv.getItem(slot))) {
                                match = false;
                                break outer;
                            }
                        }
                    }
                    if (!match) continue;

                    boolean otherEmpty = true;
                    for (StationDef.SlotDef slotDef : def.slots()) {
                        if (slotDef.role() == StationDef.SlotRole.INPUT && !coveredSlots.contains(slotDef.slotIndex())) {
                            if (!inv.getItem(slotDef.slotIndex()).isEmpty()) {
                                otherEmpty = false;
                                break;
                            }
                        }
                    }
                    if (otherEmpty) return true;
                }
            }
            return false;
        }

        if (isShapeless()) {
            Map<String, List<ItemStack>> presentStacks = new HashMap<>();
            for (StationDef.SlotDef slotDef : def.slots()) {
                if (slotDef.role() != StationDef.SlotRole.INPUT) continue;
                String group = slotDef.group() != null ? slotDef.group() : "ingredients";
                ItemStack stack = inv.getItem(slotDef.slotIndex());
                if (!stack.isEmpty()) {
                    presentStacks.computeIfAbsent(group, k -> new ArrayList<>()).add(stack);
                }
            }

            for (var entry : groupIngredients.entrySet()) {
                String group = entry.getKey();
                var required = entry.getValue();
                List<ItemStack> present = presentStacks.getOrDefault(group, Collections.emptyList());

                if (present.size() != required.size()) return false;

                List<Either<Ingredient, IngredientCost>> remaining = new ArrayList<>(required);
                for (ItemStack stack : present) {
                    boolean found = false;
                    for (var it = remaining.iterator(); it.hasNext(); ) {
                        var ing = it.next();
                        if (StationRecipe.matches(ing, stack)) {
                            it.remove();
                            found = true;
                            break;
                        }
                    }
                    if (!found) return false;
                }
            }

            for (StationDef.SlotDef slotDef : def.slots()) {
                if (slotDef.role() != StationDef.SlotRole.INPUT) continue;
                String group = slotDef.group() != null ? slotDef.group() : "ingredients";
                if (!groupIngredients.containsKey(group) && !inv.getItem(slotDef.slotIndex()).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        for (Map.Entry<Integer, Either<Ingredient, IngredientCost>> entry : ingredients.entrySet()) {
            if (!matches(entry.getValue(), inv.getItem(entry.getKey()))) return false;
        }
        return true;
    }

    public static boolean matches(Either<Ingredient, IngredientCost> inv, ItemStack stack) {
        if (inv.left().isPresent()) {
            return inv.left().orElseThrow().test(stack);
        } else if (inv.right().isPresent()) {
            var cost = inv.right().orElseThrow();
            return cost.test(stack);
        }

        return false;
    }

    public Map<Integer, Either<Ingredient, IngredientCost>> getResolvedIngredients(StationRecipeInput inv, Level level) {
        if (!isShaped()) return ingredients;
        StationDef def = Workstations.get(stationId);
        if (def == null || def.grid().isEmpty()) return Collections.emptyMap();
        int gridRows = def.grid().get().rows();
        int gridCols = def.grid().get().columns();
        int patRows = pattern.size();
        int patCols = pattern.getFirst().length();
        for (int dy = 0; dy <= gridRows - patRows; dy++) {
            for (int dx = 0; dx <= gridCols - patCols; dx++) {
                Map<Integer, Either<Ingredient, IngredientCost>> map = new HashMap<>();
                Set<Integer> coveredSlots = new HashSet<>();
                boolean ok = true;
                outer:
                for (int r = 0; r < patRows; r++) {
                    for (int c = 0; c < patCols; c++) {
                        char ch = pattern.get(r).charAt(c);
                        if (ch == ' ') continue;
                        var ing = key.get(ch);
                        if (ing == null) {
                            ok = false;
                            break outer;
                        }
                        Optional<Integer> slotIdx = def.getSlotIndexAt(dy + r, dx + c);
                        if (slotIdx.isEmpty()) {
                            ok = false;
                            break outer;
                        }
                        int slot = slotIdx.get();
                        coveredSlots.add(slot);
                        if (!StationRecipe.matches(ing, inv.getItem(slot))) {
                            ok = false;
                            break outer;
                        }
                        map.put(slot, ing);
                    }
                }
                if (!ok) continue;
                boolean otherEmpty = true;
                for (StationDef.SlotDef slotDef : def.slots()) {
                    if (slotDef.role() == StationDef.SlotRole.INPUT && !coveredSlots.contains(slotDef.slotIndex())) {
                        if (!inv.getItem(slotDef.slotIndex()).isEmpty()) {
                            otherEmpty = false;
                            break;
                        }
                    }
                }
                if (otherEmpty) return map;
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull StationRecipeInput inv) {
        return results.values().iterator().next().create();
    }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<StationRecipeInput>> getSerializer() {
        return Workstations.STATION_RECIPE_SERIALIZER;
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<StationRecipeInput>> getType() {
        return Workstations.STATION_RECIPE_TYPE;
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
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

    public static final MapCodec<StationRecipe> CODEC = new MapCodec<>() {
        @Override
        public <T> DataResult<StationRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
            T stationValue = input.get("station");
            if (stationValue == null) return DataResult.error(() -> "Missing 'station'");
            DataResult<Identifier> stationResult = Identifier.CODEC.parse(ops, stationValue);
            if (stationResult.error().isPresent()) return DataResult.error(() -> "Invalid station");
            Identifier stationId = stationResult.result().orElseThrow();

            int processingTime = 0;
            T ptValue = input.get("processing_time");
            if (ptValue != null) {
                DataResult<Integer> pt = Codec.INT.parse(ops, ptValue);
                if (pt.error().isPresent()) return DataResult.error(() -> "Invalid processing_time");
                processingTime = pt.result().orElseThrow();
            }

            StationDef def = Workstations.get(stationId);
            if (def == null) return DataResult.error(() -> "Unknown station: " + stationId);

            List<String> pattern = null;
            Map<String, Either<Ingredient, IngredientCost>> keyMap = null;

            Map<String, Either<Ingredient, IngredientCost>> nameIngredients = new HashMap<>();
            Map<String, List<Either<Ingredient, IngredientCost>>> groupIngredients = new HashMap<>();
            Map<String, ItemStackTemplate> nameResults = new HashMap<>();

            Set<String> reservedKeys = new HashSet<>(Arrays.asList(
                    "type", "station", "processing_time", "pattern", "key"
            ));

            T patternValue = input.get("pattern");
            if (patternValue != null) {

                DataResult<List<String>> patternResult = Codec.STRING.listOf().parse(ops, patternValue);
                if (patternResult.error().isPresent()) return DataResult.error(() -> "Invalid pattern");
                pattern = patternResult.result().orElseThrow();

                if (def.grid().isEmpty()) return DataResult.error(() -> "Station missing grid for shaped recipe");
                int gridRows = def.grid().get().rows();
                int gridCols = def.grid().get().columns();
                int patRows = pattern.size();
                int patCols = patRows > 0 ? pattern.getFirst().length() : 0;
                if (patRows > gridRows || patCols > gridCols) {
                    return DataResult.error(() -> "Pattern exceeds station grid dimensions");
                }

                T keyValue = input.get("key");
                if (keyValue == null) return DataResult.error(() -> "Missing key for shaped recipe");
                DataResult<Map<String, Either<Ingredient, IngredientCost>>> keyResult = ops.getMap(keyValue).flatMap(keyMap_ -> {
                    Map<String, Either<Ingredient, IngredientCost>> map = new HashMap<>();
                    for (Pair<T, T> pair : (Iterable<Pair<T, T>>) () -> keyMap_.entries().iterator()) {
                        T k = pair.getFirst();
                        T v = pair.getSecond();
                        String charStr = ops.getStringValue(k).result().orElse("");
                        DataResult<Either<Ingredient, IngredientCost>> ingResult = EITHER_CODEC.parse(ops, v);
                        if (ingResult.error().isPresent())
                            return DataResult.error(() -> "Invalid ingredient for key '" + charStr + "'");
                        map.put(charStr, ingResult.result().orElseThrow());
                    }
                    return DataResult.success(map);
                });
                if (keyResult.error().isPresent()) return DataResult.error(() -> "Invalid key");
                keyMap = keyResult.result().orElseThrow();
            }
            else {
                T ingredientsArray = input.get("ingredients");
                if (ingredientsArray != null) {
                    DataResult<List<Either<Ingredient, IngredientCost>>> ingListResult = EITHER_CODEC.listOf().parse(ops, ingredientsArray);
                    if (ingListResult.error().isPresent())
                        return DataResult.error(() -> "Invalid shapeless ingredients array");
                    var ingList = ingListResult.result().orElseThrow();
                    groupIngredients.put("ingredients", ingList);
                }
            }

            for (Pair<T, T> pair : (Iterable<Pair<T, T>>) () -> input.entries().iterator()) {
                T keyOps = pair.getFirst();
                T valueOps = pair.getSecond();
                String key = ops.getStringValue(keyOps).result().orElse("");
                if (reservedKeys.contains(key)) continue;

                Optional<StationDef.SlotRole> roleOpt = def.getSlotRoleByName(key);
                if (roleOpt.isPresent()) {

                    if (roleOpt.get() == StationDef.SlotRole.INPUT || roleOpt.get() == StationDef.SlotRole.FUEL) {
                        if (!nameIngredients.containsKey(key)) {
                            DataResult<Either<Ingredient, IngredientCost>> ingResult = EITHER_CODEC.parse(ops, valueOps);
                            if (ingResult.error().isPresent())
                                return DataResult.error(() -> "Invalid ingredient for slot '" + key + "'");

                            nameIngredients.put(key, ingResult.result().orElseThrow());
                        }
                    } else if (roleOpt.get() == StationDef.SlotRole.OUTPUT) {
                        DataResult<ItemStackTemplate> stackResult = ItemStackTemplate.CODEC.parse(ops, valueOps);
                        if (stackResult.error().isPresent())
                            return DataResult.error(() -> "Invalid result for slot '" + key + "'");

                        nameResults.put(key, stackResult.result().orElseThrow());
                    }
                } else {

                    boolean groupExists = def.slots().stream()
                            .filter(s -> s.role() == StationDef.SlotRole.INPUT)
                            .map(s -> s.group() != null ? s.group() : "ingredients")
                            .anyMatch(group -> group.equals(key));

                    if (groupExists && !groupIngredients.containsKey(key)) {
                        DataResult<List<Either<Ingredient, IngredientCost>>> ingListResult = EITHER_CODEC.listOf().parse(ops, valueOps);
                        if (ingListResult.error().isPresent()) {
                            return DataResult.error(() -> "Invalid ingredient list for group '" + key + "'");
                        }
                        groupIngredients.put(key, ingListResult.result().orElseThrow());
                    }
                }
            }

            if (pattern != null) {
                groupIngredients.clear();
            }

            Map<Integer, Either<Ingredient, IngredientCost>> resolvedIngredients = new HashMap<>();
            if (pattern == null) {
                if (!nameIngredients.isEmpty()) {
                    // named slots
                    for (var entry : nameIngredients.entrySet()) {
                        Optional<Integer> idx = def.getSlotIndex(entry.getKey());
                        if (idx.isEmpty()) return DataResult.error(() -> "No slot index for name: " + entry.getKey());
                        resolvedIngredients.put(idx.get(), entry.getValue());
                    }
                }
            }

            Map<Integer, ItemStackTemplate> resolvedResults = new HashMap<>();
            for (var entry : nameResults.entrySet()) {
                Optional<Integer> idx = def.getSlotIndex(entry.getKey());
                if (idx.isEmpty()) return DataResult.error(() -> "No slot index for name: " + entry.getKey());
                resolvedResults.put(idx.get(), entry.getValue());
            }

            // no results!
            if (resolvedResults.isEmpty()) {
                return DataResult.error(() -> "No result(s) defined for recipe");
            }

            return DataResult.success(new StationRecipe(
                    stationId,
                    resolvedIngredients,
                    resolvedResults,
                    processingTime,
                    groupIngredients,
                    pattern,
                    keyMap
            ));
        }

        @Override
        public <T> RecordBuilder<T> encode(StationRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            prefix.add(ops.createString("station"), Identifier.CODEC.encodeStart(ops, recipe.stationId));
            if (recipe.processingTime > 0) {
                prefix.add(ops.createString("processing_time"), Codec.INT.encodeStart(ops, recipe.processingTime));
            }

            StationDef def = Workstations.get(recipe.stationId);
            if (def != null) {
                if (recipe.isShaped()) {
                    prefix.add(ops.createString("pattern"), Codec.STRING.listOf().encodeStart(ops, recipe.pattern));
                    Map<String, Either<Ingredient, IngredientCost>> keyOut = new HashMap<>();
                    recipe.key.forEach((c, ing) -> keyOut.put(String.valueOf(c), ing));
                    prefix.add(ops.createString("key"), Codec.unboundedMap(Codec.STRING, EITHER_CODEC).encodeStart(ops, keyOut));
                } else if (recipe.isShapeless()) {
                    if (recipe.groupIngredients.size() == 1 && recipe.groupIngredients.containsKey("ingredients")) {
                        prefix.add(ops.createString("ingredients"), EITHER_CODEC.listOf().encodeStart(ops, recipe.groupIngredients.get("ingredients")));
                    } else {
                        for (var entry : recipe.groupIngredients.entrySet()) {
                            prefix.add(ops.createString(entry.getKey()), EITHER_CODEC.listOf().encodeStart(ops, entry.getValue()));
                        }
                    }
                } else if (!recipe.ingredients.isEmpty()) {
                    for (var entry : recipe.ingredients.entrySet()) {
                        Optional<String> name = def.getSlotName(entry.getKey());
                        name.ifPresent(s -> prefix.add(ops.createString(s), EITHER_CODEC.encodeStart(ops, entry.getValue())));
                    }
                }

                for (var entry : recipe.results.entrySet()) {
                    Optional<String> name = def.getSlotName(entry.getKey());
                    name.ifPresent(s -> prefix.add(ops.createString(s), ItemStackTemplate.CODEC.encodeStart(ops, entry.getValue())));
                }
            }
            return prefix;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("station"), ops.createString("processing_time"));
        }
    };
}