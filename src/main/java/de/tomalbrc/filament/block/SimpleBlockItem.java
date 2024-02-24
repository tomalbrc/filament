package de.tomalbrc.filament.block;

import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class SimpleBlockItem extends CustomBlockItem implements PolymerItem {
    private final PolymerModelData polymerModel;

    public SimpleBlockItem(Properties properties, Block block, BlockData data) {
        super(block, properties);
        this.polymerModel = PolymerResourcePackUtils.requestModel(
                data.properties().itemBase,
                data.itemResource() != null && data.itemResource().models() != null ? data.itemResource().models().get("default") : data.blockResource().models().entrySet().iterator().next().getValue()
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, ServerPlayer player) {
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, ServerPlayer player) {
        return this.polymerModel.value();
    }
}