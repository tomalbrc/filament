package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.decoration.holder.SimpleHolder;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.BlockWithMovingElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDecorationBlock extends DecorationBlock implements BlockWithMovingElementHolder {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);

    public SimpleDecorationBlock(Properties properties, ResourceLocation decorationId) {
        super(properties, decorationId);

        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(ROTATION, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ROTATION);
    }

    @Nullable
    public ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new SimpleHolder();
    }

    @Override
    @NotNull
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        if (this.getDecorationData().properties().drops) {
            return List.of(BuiltInRegistries.ITEM.get(this.decorationId).getDefaultInstance());
        }
        return List.of();
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!this.getDecorationData().hasBlocks()) {
            SoundEvent breakSound = this.getDecorationData().properties().blockBase.defaultBlockState().getSoundType().getBreakSound();
            level.playSound(null, blockPos,  breakSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (this.getDecorationData().properties().showBreakParticles)
            Util.showBreakParticle((ServerLevel) level, blockPos, this.getDecorationData().properties().useItemParticles ? BuiltInRegistries.ITEM.get(this.decorationId).getDefaultInstance() : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());

        if (!level.isClientSide() && this.getDecorationData().properties().drops) {
            for (ItemStack drop : this.getDrops(blockState, null)) {
                Util.spawnAtLocation(level, blockPos.getCenter(), drop);
            }
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }
}
