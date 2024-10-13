package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class SimpleBlockItem extends SimpleItem implements PolymerItem, BehaviourHolder {
    private final BlockData blockData;

    public SimpleBlockItem(Properties properties, Block block, BlockData data) {
        super(block, properties, data.properties(), data.vanillaItem());
        this.blockData = data;
        this.initBehaviours(data.behaviourConfig());
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.vanillaItem;
    }

    @Override
    protected Map<String, ResourceLocation> getModelMap() {
        return this.blockData.itemResource() == null ? Map.of() : this.blockData.itemResource().models();
    }

    @Override
    protected ResourceLocation getModel() {
        boolean hasItemModels = this.blockData.itemResource() != null && this.blockData.itemResource().models() != null;
        return hasItemModels ? this.blockData.itemResource().models().get("default") : this.blockData.blockResource().models().entrySet().iterator().next().getValue().model();
    }
}