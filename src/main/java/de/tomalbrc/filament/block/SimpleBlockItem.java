package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviours.item.Cosmetic;
import de.tomalbrc.filament.behaviours.item.Fuel;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.registry.FuelRegistry;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class SimpleBlockItem extends CustomBlockItem implements PolymerItem, Equipable {
    private final PolymerModelData polymerModel;

    private final BlockData blockData;

    public SimpleBlockItem(Properties properties, Block block, BlockData data) {
        super(block, properties);
        this.blockData = data;
        this.polymerModel = PolymerResourcePackUtils.requestModel(
                data.properties().itemBase,
                data.itemResource() != null && data.itemResource().models() != null ? data.itemResource().models().get("default") : data.blockResource().models().entrySet().iterator().next().getValue()
        );

        if (data.isFuel()) {
            Fuel.FuelConfig fuel = this.blockData.behaviour().get(Constants.Behaviours.FUEL);
            FuelRegistry.add(this, fuel.value);
        }
    }

    public BlockData getBlockData() {
        return this.blockData;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, ServerPlayer player) {
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, ServerPlayer player) {
        return this.polymerModel.value();
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        var res = super.use(level, user, hand);

        if (res.getResult() != InteractionResult.CONSUME && this.blockData.isCosmetic()) {
            res = this.swapWithEquipmentSlot(this, level, user, hand);
        }

        return res;
    }

    @Override
    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        if (blockData.isCosmetic()) {
            Cosmetic.CosmeticConfig cosmetic = blockData.behaviour().get(Constants.Behaviours.COSMETIC);
            return cosmetic.slot;
        }
        return EquipmentSlot.MAINHAND;
    }
}