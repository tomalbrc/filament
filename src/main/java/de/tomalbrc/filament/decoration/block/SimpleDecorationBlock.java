package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDecorationBlock extends DecorationBlock implements BlockWithElementHolder {
    public SimpleDecorationBlock(Properties properties, DecorationData data) {
        super(properties, data);
    }

    @Nullable
    public ElementHolder createElementHolder(ServerLevel world, BlockPos blockPos, BlockState initialBlockState) {
        DecorationHolder holder = new DecorationHolder(() -> visualItemStack(world, blockPos, initialBlockState));
        DecorationUtil.setupElements(holder, this.getDecorationData(), Direction.UP, this.getVisualRotationYInDegrees(initialBlockState), this.visualItemStack(world, blockPos, initialBlockState), null);
        return holder;
    }

    @Override
    @NotNull
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockState returnVal = super.playerWillDestroy(level, blockPos, blockState, player);
        if (!player.hasInfiniteMaterials()) this.playerDestroy(level, player, blockPos, blockState, null, player.getMainHandItem());
        return returnVal;
    }

    @Override
    public ItemStack visualItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        var item = BuiltInRegistries.ITEM.get(this.data.id()).getDefaultInstance();

        if (stateMap != null && cmdMap != null && item.getItem() instanceof FilamentItem filamentItem) {
            var val = stateMap.get(behaviourModifiedBlockState(blockState, blockState));
            if (val != null && cmdMap.containsKey(val)) {

                var v = filamentItem.getModelData().get(cmdMap.get(val));
                if (v != null) item.set(DataComponents.CUSTOM_MODEL_DATA, v.asComponent());
            }
        }

        return item;
    }

    @Override
    @NotNull
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        if (this.getDecorationData().properties().drops) {
            List<ItemStack> list = ObjectArrayList.of(BuiltInRegistries.ITEM.get(this.decorationId).getDefaultInstance());
            list.addAll(super.getDrops(blockState, builder));
            return list;
        } else {
            return super.getDrops(blockState, builder);
        }
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) return;

        BlockUtil.playBreakSound(level, blockPos, blockState);

        if (data.properties().showBreakParticles)
            DecorationUtil.showBreakParticle((ServerLevel) level, data.properties().useItemParticles ? BuiltInRegistries.ITEM.get(this.decorationId).getDefaultInstance() : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());
    }
}
