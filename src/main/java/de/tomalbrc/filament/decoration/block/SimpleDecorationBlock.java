package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.holder.SimpleDecorationHolder;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDecorationBlock extends DecorationBlock implements BlockWithElementHolder {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);

    public SimpleDecorationBlock(Properties properties, ResourceLocation decorationId) {
        super(properties, decorationId);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }

    @Nullable
    public ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new SimpleDecorationHolder();
    }

    @Override
    @NotNull
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockState returnVal = super.playerWillDestroy(level, blockPos, blockState, player);
        this.playerDestroy(level, player, blockPos, blockState, null, player.getMainHandItem());
        return returnVal;
    }

    @Override
    @NotNull
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        if (this.getDecorationData().properties().drops) {
            List<ItemStack> list = ObjectArrayList.of(BuiltInRegistries.ITEM.getValue(this.decorationId).getDefaultInstance());
            list.addAll(super.getDrops(blockState, builder));
            return list;
        }
        return List.of();
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        if (!(level instanceof ServerLevel)) return;

        DecorationData data = this.getDecorationData();
        if (!data.hasBlocks()) {
            BlockUtil.playBreakSound(level, blockPos, blockState);
        }

        if (data.properties().showBreakParticles)
            DecorationUtil.showBreakParticle((ServerLevel) level, data.properties().useItemParticles ? BuiltInRegistries.ITEM.getValue(this.decorationId).getDefaultInstance() : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());
    }
}
