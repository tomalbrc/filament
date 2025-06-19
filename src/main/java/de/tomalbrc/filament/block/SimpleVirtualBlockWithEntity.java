package de.tomalbrc.filament.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import de.tomalbrc.filament.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SimpleVirtualBlockWithEntity extends SimpleVirtualBlock implements EntityBlock {
    public SimpleVirtualBlockWithEntity(Properties properties, BlockData data) {
        super(properties, data);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof BlockBehaviourWithEntity<?> behaviourWithEntity) {
                var res = behaviourWithEntity.newBlockEntity(blockPos, blockState);
                if (res != null)
                    return res;
            }
        }

        return null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide)
            return null;

        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof BlockBehaviourWithEntity<?> behaviourWithEntity) {
                var res = behaviourWithEntity.getTicker(level, blockState, blockEntityType);
                if (res != null)
                    return res;
            }
        }

        return null;
    }
}
