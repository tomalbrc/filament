package de.tomalbrc.filament.recipe;

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
        Map<Integer, Ingredient> ingredients,

        Map<Integer, ItemStackTemplate> results, // resolved slot -> result
        int processingTime,
        List<Ingredient> shapelessIngredients,// non‑null only for shapeless
        List<String> pattern, // non‑null only for shaped
        Map<Character, Ingredient> key // non‑null only for shaped
) implements Recipe<StationRecipeInput> {

    public boolean isShapeless() {
        return shapelessIngredients != null && !shapelessIngredients.isEmpty();
    }

    public boolean isShaped() {
        return pattern != null && key != null;
    }

    @Override
    public boolean matches(@NonNull StationRecipeInput inv, @NonNull Level level) {
        StationDef def = Workstations.get(stationId);
        if (def == null) return false;

        if (isShapeless()) {
            List<Integer> inputSlots = def.slots().stream()
                    .filter(s -> s.role() == StationDef.SlotRole.INPUT)
                    .map(StationDef.SlotDef::slotIndex)
                    .sorted()
                    .toList();
            List<ItemStack> inputStacks = inputSlots.stream()
                    .map(inv::getItem)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (inputStacks.size() != shapelessIngredients.size()) return false;
            List<Ingredient> remaining = new ArrayList<>(shapelessIngredients);
            for (ItemStack stack : inputStacks) {
                boolean found = false;
                for (Iterator<Ingredient> it = remaining.iterator(); it.hasNext(); ) {
                    Ingredient ing = it.next();
                    if (ing.test(stack)) {
                        it.remove();
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        } else if (isShaped()) {
            if (def.grid().isEmpty()) return false;
            int gridRows = def.grid().get().rows();
            int gridCols = def.grid().get().columns();
            int patRows = pattern.size();
            int patCols = pattern.get(0).length();
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
                            Ingredient ing = key.get(ch);
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
                            if (!ing.test(inv.getItem(slot))) {
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
        } else {
            for (Map.Entry<Integer, Ingredient> entry : ingredients.entrySet()) {
                if (!entry.getValue().test(inv.getItem(entry.getKey()))) return false;
            }
            return true;
        }
    }

    public Map<Integer, Ingredient> getResolvedIngredients(StationRecipeInput inv, Level level) {
        if (!isShaped()) return ingredients;
        StationDef def = Workstations.get(stationId);
        if (def == null || def.grid().isEmpty()) return Collections.emptyMap();
        int gridRows = def.grid().get().rows();
        int gridCols = def.grid().get().columns();
        int patRows = pattern.size();
        int patCols = pattern.get(0).length();
        for (int dy = 0; dy <= gridRows - patRows; dy++) {
            for (int dx = 0; dx <= gridCols - patCols; dx++) {
                Map<Integer, Ingredient> map = new HashMap<>();
                Set<Integer> coveredSlots = new HashSet<>();
                boolean ok = true;
                outer:
                for (int r = 0; r < patRows; r++) {
                    for (int c = 0; c < patCols; c++) {
                        char ch = pattern.get(r).charAt(c);
                        if (ch == ' ') continue;
                        Ingredient ing = key.get(ch);
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
                        if (!ing.test(inv.getItem(slot))) {
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
            Identifier stationId = stationResult.result().get();

            int processingTime = 0;
            T ptValue = input.get("processing_time");
            if (ptValue != null) {
                DataResult<Integer> pt = Codec.INT.parse(ops, ptValue);
                if (pt.error().isPresent()) return DataResult.error(() -> "Invalid processing_time");
                processingTime = pt.result().get();
            }

            StationDef def = Workstations.get(stationId);
            if (def == null) return DataResult.error(() -> "Unknown station: " + stationId);

            Map<String, Ingredient> nameIngredients = new HashMap<>();
            Map<String, ItemStackTemplate> nameResults = new HashMap<>();
            List<Ingredient> shapelessList = new ArrayList<>();
            List<String> pattern = null;
            Map<Character, Ingredient> keyMap = null;

            T patternValue = input.get("pattern");
            T ingredientsArray = input.get("ingredients");
            Set<String> reservedKeys = new HashSet<>(Arrays.asList("type", "station", "processing_time"));

            if (patternValue != null) {
                DataResult<List<String>> patternResult = Codec.STRING.listOf().parse(ops, patternValue);
                if (patternResult.error().isPresent()) return DataResult.error(() -> "Invalid pattern");
                pattern = patternResult.result().get();
                int patRows = pattern.size();
                int patCols = patRows > 0 ? pattern.get(0).length() : 0;

                if (def.grid().isEmpty()) return DataResult.error(() -> "Station missing grid for shaped recipe");
                int gridRows = def.grid().get().rows();
                int gridCols = def.grid().get().columns();
                if (patRows > gridRows || patCols > gridCols) {
                    return DataResult.error(() -> "Pattern exceeds station grid dimensions");
                }

                T keyValue = input.get("key");
                if (keyValue == null) return DataResult.error(() -> "Missing key for shaped recipe");
                DataResult<Map<Character, Ingredient>> keyResult = ops.getMap(keyValue).flatMap(keyMap_ -> {
                    Map<Character, Ingredient> map = new HashMap<>();
                    for (Pair<T, T> pair : (Iterable<Pair<T, T>>) () -> keyMap_.entries().iterator()) {
                        T k = pair.getFirst();
                        T v = pair.getSecond();
                        String charStr = ops.getStringValue(k).result().orElse("");
                        if (charStr.length() != 1) return DataResult.error(() -> "Invalid key character");
                        char c = charStr.charAt(0);
                        DataResult<Ingredient> ingResult = Ingredient.CODEC.parse(ops, v);
                        if (ingResult.error().isPresent())
                            return DataResult.error(() -> "Invalid ingredient for key '" + c + "'");
                        map.put(c, ingResult.result().get());
                    }
                    return DataResult.success(map);
                });
                if (keyResult.error().isPresent()) return DataResult.error(() -> "Invalid key");
                keyMap = keyResult.result().get();
                reservedKeys.add("pattern");
                reservedKeys.add("key");
            }
            else if (ingredientsArray != null) {
                DataResult<List<Ingredient>> ingListResult = Ingredient.CODEC.listOf().parse(ops, ingredientsArray);
                if (ingListResult.error().isPresent())
                    return DataResult.error(() -> "Invalid shapeless ingredients array");
                List<Ingredient> ingList = ingListResult.result().get();

                List<StationDef.SlotDef> inputSlots = def.slots().stream()
                        .filter(s -> s.role() == StationDef.SlotRole.INPUT)
                        .sorted(Comparator.comparingInt(StationDef.SlotDef::slotIndex))
                        .toList();
                for (int i = 0; i < ingList.size(); i++) {
                    nameIngredients.put(inputSlots.get(i).name(), ingList.get(i));
                }
                shapelessList.addAll(ingList);
                reservedKeys.add("ingredients");
            }

            for (Pair<T, T> pair : (Iterable<Pair<T, T>>) () -> input.entries().iterator()) {
                T keyOps = pair.getFirst();
                T valueOps = pair.getSecond();
                String key = ops.getStringValue(keyOps).result().orElse("");
                if (reservedKeys.contains(key)) continue;

                Optional<StationDef.SlotRole> roleOpt = def.getSlotRoleByName(key);
                if (roleOpt.isEmpty()) return DataResult.error(() -> "Unknown slot name: " + key);

                if (roleOpt.get() == StationDef.SlotRole.INPUT || roleOpt.get() == StationDef.SlotRole.FUEL) {
                    if (!nameIngredients.containsKey(key)) {
                        DataResult<Ingredient> ingResult = Ingredient.CODEC.parse(ops, valueOps);

                        if (ingResult.error().isPresent())
                            return DataResult.error(() -> "Invalid ingredient for slot '" + key + "'");

                        nameIngredients.put(key, ingResult.result().get());
                    }
                } else if (roleOpt.get() == StationDef.SlotRole.OUTPUT) {
                    DataResult<ItemStackTemplate> stackResult = ItemStackTemplate.CODEC.parse(ops, valueOps);

                    if (stackResult.error().isPresent())
                        return DataResult.error(() -> "Invalid result for slot '" + key + "'");

                    nameResults.put(key, stackResult.result().get());
                }
            }

            Map<Integer, Ingredient> resolvedIngredients = new HashMap<>();
            if (patternValue == null) { // not shaped
                for (var entry : nameIngredients.entrySet()) {
                    Optional<Integer> idx = def.getSlotIndex(entry.getKey());
                    if (idx.isEmpty()) return DataResult.error(() -> "No slot index for name: " + entry.getKey());
                    resolvedIngredients.put(idx.get(), entry.getValue());
                }
            }

            Map<Integer, ItemStackTemplate> resolvedResults = new HashMap<>();
            for (var entry : nameResults.entrySet()) {
                Optional<Integer> idx = def.getSlotIndex(entry.getKey());
                if (idx.isEmpty()) return DataResult.error(() -> "No slot index for name: " + entry.getKey());
                resolvedResults.put(idx.get(), entry.getValue());
            }

            return DataResult.success(new StationRecipe(stationId, resolvedIngredients, resolvedResults,
                    processingTime, shapelessList, pattern, keyMap));
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

                    Map<String, Ingredient> keyOut = new HashMap<>();
                    recipe.key.forEach((c, ing) -> keyOut.put(String.valueOf(c), ing));

                    prefix.add(ops.createString("key"), Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).encodeStart(ops, keyOut));
                } else if (recipe.isShapeless()) {

                    List<Ingredient> ingList = new ArrayList<>();
                    List<Integer> inputSlots = def.slots().stream()
                            .filter(s -> s.role() == StationDef.SlotRole.INPUT)
                            .map(StationDef.SlotDef::slotIndex)
                            .sorted()
                            .toList();

                    for (int slot : inputSlots) {
                        Ingredient ing = recipe.ingredients.get(slot);
                        if (ing != null) ingList.add(ing);
                    }

                    if (!ingList.isEmpty()) {
                        prefix.add(ops.createString("ingredients"), Ingredient.CODEC.listOf().encodeStart(ops, ingList));
                    }
                } else {
                    // positional
                    for (var entry : recipe.ingredients.entrySet()) {
                        Optional<String> name = def.getSlotName(entry.getKey());
                        name.ifPresent(s -> prefix.add(ops.createString(s), Ingredient.CODEC.encodeStart(ops, entry.getValue())));
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