package de.tomalbrc.filament.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.behaviours.BehaviourHolder;
import de.tomalbrc.filament.behaviours.BehaviourMap;
import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class SimpleBlock extends Block implements PolymerTexturedBlock, BehaviourHolder, SimpleWaterloggedBlock {
    protected Map<BlockState, BlockData.BlockStateMeta> stateMap;
    protected final BlockState breakEventState;
    protected final BlockData blockData;

    protected final StateDefinition<Block, BlockState> stateDefinitionEx;

    private final BehaviourMap behaviours = new BehaviourMap();

    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    public SimpleBlock(BlockBehaviour.Properties properties, BlockData data) {
        super(properties);

        this.initBehaviours(data.behaviourConfig());
        this.breakEventState = data.properties().blockBase.defaultBlockState();
        this.blockData = data;

        // the StateDefinition built too early, cant access BlockData from within createBlockStateDefinition
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinitionEx = builder.create(Block::defaultBlockState, BlockState::new);

        BlockState[] def = {this.stateDefinitionEx.any()};
        this.forEach(behaviour -> {
            def[0] = behaviour.modifyDefaultState(def[0]);
        });
        this.registerDefaultState(def[0]);
    }

    public boolean hasData() {
        return this.blockData != null;
    }

    @Override
    @NotNull
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return stateDefinitionEx;
    }

    public void postRegister() {
        this.stateMap = this.blockData.createStandardStateMap();
        this.forEach(behaviour -> {
            behaviour.modifyStateMap(this.stateMap, this.blockData);
        });
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                BlockState polyBlockState = blockBehaviour.getCustomPolymerBlockState(this.stateMap, blockState);
                if (polyBlockState != null)
                    return polyBlockState;
            }
        }
        return this.stateMap.get(blockState).blockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    private void forEach(Consumer<de.tomalbrc.filament.api.behaviour.BlockBehaviour<?>> consumer) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                consumer.accept(blockBehaviour);
            }
        }
    }

    /// --- behaviour impl

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (this.getBehaviours() != null)
            this.forEach(x -> x.createBlockStateDefinition(builder));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState def = this.defaultBlockState();
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                def = blockBehaviour.getStateForPlacement(def, blockPlaceContext);
            }
        }

        return def;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        this.forEach(x -> x.neighborChanged(blockState, level, blockPos, block, blockPos2, bl));
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.rotate(blockState, rotation);
                if (res != null)
                    return res;
            }
        }
        return super.rotate(blockState, rotation);
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.mirror(blockState, mirror);
                if (res != null)
                    return res;
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.isSignalSource(blockState);
                if (res)
                    return true;
            }
        }
        return super.isSignalSource(blockState);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getDirectSignal(blockState, blockGetter, blockPos, direction);
                if (res > 0)
                    return res;
            }
        }
        return super.getDirectSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getSignal(blockState, blockGetter, blockPos, direction);
                if (res > 0)
                    return res;
            }
        }
        return super.getSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        if (this.getBehaviours() != null) for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.useShapeForLightOcclusion(blockState);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.useShapeForLightOcclusion(blockState);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.canBeReplaced(blockState, blockPlaceContext);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    @NotNull
    protected FluidState getFluidState(BlockState blockState) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.getFluidState(blockState);
                if (res != null)
                    return res;
            }
        }
        return super.getFluidState(blockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour && blockBehaviour instanceof SimpleWaterloggedBlock waterloggedBlock) {
                return waterloggedBlock.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour && blockBehaviour instanceof SimpleWaterloggedBlock waterloggedBlock) {
                return waterloggedBlock.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid);
            }
        }
        return false;
    }

    @Override
    @NotNull
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
                if (res != null) {
                    return res;
                }
            }
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof de.tomalbrc.filament.api.behaviour.BlockBehaviour<?> blockBehaviour) {
                var res = blockBehaviour.isPathfindable(blockState, pathComputationType);
                if (res.isPresent())
                    return res.get();
            }
        }
        return super.isPathfindable(blockState, pathComputationType);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        this.forEach(x -> x.onPlace(blockState, level, blockPos, blockState2, bl));
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.forEach(x -> x.onRemove(blockState, level, blockPos, blockState2, bl));
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        super.tick(blockState, serverLevel, blockPos, randomSource);
        this.forEach(x -> x.tick(blockState, serverLevel, blockPos, randomSource));
    }
}
