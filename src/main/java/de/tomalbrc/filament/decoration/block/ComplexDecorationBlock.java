package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComplexDecorationBlock extends DecorationBlock implements EntityBlock {
    public ComplexDecorationBlock(Properties properties, DecorationData decorationData) {
        super(properties, decorationData);
    }

    @Override
    public ItemStack visualItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        var item = ((DecorationBlockEntity) Objects.requireNonNull(levelReader.getBlockEntity(blockPos))).visualItemStack(blockState);

        if (stateMap != null && cmdMap != null) {
            var val = stateMap.get(behaviourModifiedBlockState(blockState, blockState));
            if (val != null && cmdMap.containsKey(val)) {
                item.set(DataComponents.ITEM_MODEL, data.id().withPrefix("block/"));
                item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(cmdMap.get(val)), List.of()));
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
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        ItemStack stack;
        BlockEntity blockEntity = levelReader.getBlockEntity(blockPos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            stack = decorationBlockEntity.getMainBlockEntity().getItem();
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : decorationBlockEntity.getBehaviours()) {
                if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                    stack = decorationBehaviour.getCloneItemStack(stack, levelReader, blockPos, blockState, includeData);
                }
            }
        } else {
            stack = super.getCloneItemStack(levelReader, blockPos, blockState, includeData);
        }
        return stack;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        blockState = super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);

        if (levelReader.getBlockEntity(blockPos) instanceof DecorationBlockEntity blockEntity) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : blockEntity.getBehaviours()) {
                if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                    decorationBehaviour.updateShape(blockEntity, blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
                }
            }
        }

        return blockState;
    }
}
