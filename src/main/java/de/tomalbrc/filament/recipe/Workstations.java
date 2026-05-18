package de.tomalbrc.filament.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Workstations {
    static Map<Identifier, StationDef> stations = new ConcurrentHashMap<>();

    public static void init() {
    }

    public static final RecipeSerializer<StationRecipe> STATION_RECIPE_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "station_recipe"),
            new RecipeSerializer<>(StationRecipe.CODEC, null)
    );

    public static final RecipeType<StationRecipe> STATION_RECIPE_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "station_recipe"),
            new RecipeType<StationRecipe>() {
            }
    );

    public static StationDef get(Identifier stationId) {
        return stations.get(stationId);
    }

    public static void add(InputStream inputStream) {
        var station = StationDef.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(inputStream)));
        station.resultOrPartial().ifPresent(x -> {
            var def = x.getFirst();
            stations.put(def.id(), def);
        });
    }
}
