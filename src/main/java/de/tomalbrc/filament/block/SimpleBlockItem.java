package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

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
        return this.blockData.itemResource() == null ? this.blockData.blockResource().getModels() : Objects.requireNonNull(this.blockData.itemResource()).models();
    }
}