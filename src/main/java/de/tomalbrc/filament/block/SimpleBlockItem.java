package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviours.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class SimpleBlockItem extends SimpleItem implements PolymerItem, Equipable, BehaviourHolder {

    private final PolymerModelData itemModelData;

    private final BlockData blockData;

    public SimpleBlockItem(Properties properties, Block block, BlockData data) {
        super(block, properties, data.properties(), data.vanillaItem());
        this.blockData = data;
        boolean hasItemModels = data.itemResource() != null && data.itemResource().models() != null;
        this.itemModelData = PolymerResourcePackUtils.requestModel(
                data.vanillaItem(),
                hasItemModels ? data.itemResource().models().get("default") : data.blockResource().models().entrySet().iterator().next().getValue());

        this.initBehaviours(data.behaviourConfig());
    }

    public BlockData getBlockData() {
        return this.blockData;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, ServerPlayer player) {
        return this.itemModelData.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, ServerPlayer player) {
        return this.itemModelData.value();
    }
}