package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleBlockItem extends SimpleItem implements PolymerItem, BehaviourHolder {
    private final BlockData blockData;

    public SimpleBlockItem(Properties properties, Block block, BlockData data) {
        super(block, properties, data.properties(), data.vanillaItem());
        this.blockData = data;
        this.initBehaviours(data.behaviour());
    }

    @Override
    protected Map<String, ResourceLocation> getModelMap() {
        return this.blockData.itemResource() == null ? Map.of() : Objects.requireNonNull(this.blockData.itemResource()).models();
    }

    @Override
    protected CustomModelData getModel() {
        if (this.blockData.itemResource() != null)
            return null; // default model, not modified
        else
            return new CustomModelData(List.of(), List.of(), List.of(this.blockData.blockResource().models().entrySet().iterator().next().getKey()), List.of());
    }
}