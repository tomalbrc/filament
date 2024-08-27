package de.tomalbrc.filament.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.tomalbrc.filament.behaviours.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;


public record BlockData(
        @NotNull ResourceLocation id,
        @NotNull BlockResource blockResource,
        @NotNull ItemResource itemResource,
        @NotNull BlockModelType blockModelType,
        @NotNull BlockProperties properties,
        @Nullable BlockType type,
        @Nullable BehaviourConfigMap behaviour,
        @Nullable DataComponentMap components
        ) {


    public HashMap<String, BlockState> createStateMap() {
        HashMap<String, BlockState> val = new HashMap<>();

        if (blockResource.couldGenerate()) {
            //val = BlockModelGenerator.generate(blockResource);
            throw new UnsupportedOperationException("Not implemented");
        }
        else if (blockResource.models() != null) {
            for (HashMap.Entry<String, ResourceLocation> entry : this.blockResource.models().entrySet()) {
                PolymerBlockModel blockModel = PolymerBlockModel.of(entry.getValue());

                var requestedState = PolymerBlockResourceUtils.requestBlock(this.blockModelType, blockModel);
                val.put(entry.getKey(), requestedState);

                if (requestedState.hasProperty(SlabBlock.WATERLOGGED) && requestedState.hasProperty(SlabBlock.TYPE)) {
                    PolymerBlockResourceUtils.requestBlock(this.blockModelType, blockModel);
                }
            }
        }

        return val;
    }

    public boolean isPowersource() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.POWERSOURCE) != null;
    }

    public boolean isRepeater() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.REPEATER) != null;
    }
    public boolean isStrippable() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.STRIPPABLE) != null;
    }

    public boolean isFuel() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.FUEL) != null;
    }

    public boolean isCosmetic() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.COSMETIC) != null;
    }

    public record BlockType(ResourceLocation resourceLocation) {
        public static BlockType ofFilament(String path) {
            return new BlockType(ResourceLocation.fromNamespaceAndPath("filament", path));
        }

        public static BlockType of(String path) {
            return new BlockType(ResourceLocation.parse(path));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (BlockType.class == this.getClass() && ((BlockType)obj).resourceLocation() != null && this.resourceLocation != null)
                return ((BlockType)obj).resourceLocation().equals(this.resourceLocation);

            return false;
        }

        @Override
        public int hashCode() {
            return resourceLocation.hashCode();
        }

        public static class Deserializer implements JsonDeserializer<BlockType> {
            @Override
            public BlockType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                String resLoc = jsonElement.getAsString();
                ResourceLocation resourceLocation;
                if (resLoc.contains(":"))
                    return BlockType.of(resLoc);
                else
                    return BlockType.ofFilament(resLoc);
            }
        }
    }
}
