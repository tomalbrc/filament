package de.tomalbrc.filament.data;

import de.tomalbrc.filament.data.behaviours.block.BlockBehaviourList;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;


public record BlockData(
        @NotNull ResourceLocation id,
        @NotNull BlockResource blockResource,
        @NotNull ItemResource itemResource,
        @NotNull BlockModelType blockModelType,
        @NotNull BlockProperties properties,
        @Nullable BlockType type,
        @Nullable BlockBehaviourList behaviour
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
        return this.behaviour != null && this.behaviour.powersource != null;
    }

    public boolean isRepeater() {
        return this.behaviour != null && this.behaviour.repeater != null;
    }

    public boolean isFuel() {
        return this.behaviour != null && this.behaviour.fuel != null;
    }

    public boolean isCosmetic() {
        return this.behaviour != null && this.behaviour.cosmetic != null;
    }

    public enum BlockType {
        block,
        column,
        count,
        powerlevel,
        powered_directional,
        slab,
        directional, // not supported yet
        horizontal_directional // not supported yet
    }
}
