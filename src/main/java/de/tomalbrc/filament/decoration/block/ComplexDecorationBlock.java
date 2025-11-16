package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class ComplexDecorationBlock extends DecorationBlock implements EntityBlock {
    public ComplexDecorationBlock(Properties properties, DecorationData decorationData) {
        super(properties, decorationData);
    }

    @Override
    public ItemStack visualItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        var item = ((DecorationBlockEntity) Objects.requireNonNull(levelReader.getBlockEntity(blockPos))).visualItemStack(blockState);

        if (stateMap != null && cmdMap != null && item.getItem() instanceof FilamentItem filamentItem) {
            var val = stateMap.get(behaviourModifiedBlockState(blockState, blockState));
            if (val != null && cmdMap.containsKey(val)) {
                var v = filamentItem.getModelData().get(cmdMap.get(val));
                if (v != null) item.set(DataComponents.CUSTOM_MODEL_DATA, v.asComponent());
            }
        }

        return item;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DecorationBlockEntity(blockPos, blockState);
    }

    @Override
    @NotNull
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        ItemStack stack;
        BlockEntity blockEntity = levelReader.getBlockEntity(blockPos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            stack = decorationBlockEntity.getMainBlockEntity().getItem();
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : decorationBlockEntity.getBehaviours()) {
                if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                    stack = decorationBehaviour.getCloneItemStack(stack, levelReader, blockPos, blockState, false);
                }
            }
        } else {
            stack = super.getCloneItemStack(levelReader, blockPos, blockState);
        }
        return stack;
    }
}
