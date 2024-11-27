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
        return this.blockData.itemResource() == null ? Map.of() : Objects.requireNonNull(this.blockData.itemResource()).models();
    }

    @Override
    protected ResourceLocation getModel() {
        boolean hasItemModels = this.blockData.itemResource() != null && Objects.requireNonNull(this.blockData.itemResource()).models() != null;
        return hasItemModels ? Objects.requireNonNull(this.blockData.itemResource()).models().get("default") : removeItemPrefix(this.blockData.blockResource().models().entrySet().iterator().next().getValue().model());
    }

    private static ResourceLocation removeItemPrefix(ResourceLocation resourceLocation) {
        var path = resourceLocation.getPath();
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), path.startsWith("item/") ? path.substring("item/".length()) : path);
    }
}