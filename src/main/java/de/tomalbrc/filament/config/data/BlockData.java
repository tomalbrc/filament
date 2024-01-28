package de.tomalbrc.filament.config.data;

import de.tomalbrc.filament.config.data.behaviour.BlockBehaviourList;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.tomalbrc.filament.config.data.properties.BlockProperties;

import java.util.HashMap;


public record BlockData(
        @NotNull ResourceLocation id,
        @NotNull HashMap<String, ResourceLocation> models,
        @NotNull ResourceLocation itemModel,
        @NotNull BlockModelType type,
        @NotNull BlockProperties properties,
        @Nullable String states,
        @Nullable BlockBehaviourList behaviour
        ) {
    public HashMap<String, BlockState> createStateMap() {
        HashMap<String, BlockState> val = new HashMap<>();
        for (HashMap.Entry<String, ResourceLocation> entry : this.models.entrySet()) {
            PolymerBlockModel blockModel = PolymerBlockModel.of(entry.getValue());
            val.put(entry.getKey(), PolymerBlockResourceUtils.requestBlock(this.type, blockModel));
        }
        return val;
    }

    public boolean hasState(String stateName) {
        return this.states() != null && this.states().contains(stateName);
    }

    public boolean isPowersource() {
        return this.behaviour != null && this.behaviour.powersource != null;
    }

    public boolean isRepeater() {
        return this.behaviour != null && this.behaviour.repeater != null;
    }
}
