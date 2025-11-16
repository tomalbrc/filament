package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviour for redstone power source
 */
public class Repeater implements BlockBehaviour<Repeater.Config> {
    private final Config config;

    public Repeater(Config config) {
        this.config = config;
    }

    public static final IntegerProperty SIGNAL = IntegerProperty.create("signal", 0, 15);

    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(SIGNAL, 0).setValue(POWERED, false);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SIGNAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockState block, BlockPlaceContext blockPlaceContext) {
        Direction dir = blockPlaceContext.getNearestLookingDirection();
        BlockState state = block.setValue(FACING, dir);
        int s = this.getInputSignal(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), state);
        state = state.setValue(POWERED, s > 0).setValue(SIGNAL, s);
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.updateNeighborsInFront(level, pos, state);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockGetter.getBlockState(blockPos.relative(direction)).isRedstoneConductor(blockGetter, blockPos.relative(direction)) ? blockState.getSignal(blockGetter, blockPos, direction) : 0;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED)) {
            return 0;
        } else {
            return blockState.getValue(FACING) == direction ? blockState.getValue(SIGNAL)-this.config.loss : 0;
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        this.checkTickOnNeighbor(blockState.getBlock(), level, blockPos, blockState);
    }

    protected void checkTickOnNeighbor(Block self, Level level, BlockPos blockPos, BlockState blockState) {
        boolean powered = blockState.getValue(POWERED);
        int power = blockState.getValue(SIGNAL);
        int s = this.getInputSignal(level, blockPos, blockState);
        if ((s != power || powered != s > 0) && !level.getBlockTicks().willTickThisTick(blockPos, self)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.shouldPrioritize(level, blockPos, blockState)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (powered) {
                tickPriority = TickPriority.VERY_HIGH;
            }

            level.scheduleTick(blockPos, self, this.config.delay, tickPriority);
        }
    }

    public static boolean isRelay(BlockState blockState) {
        return blockState.getBlock() instanceof BehaviourHolder behaviourHolder && behaviourHolder.has(Behaviours.REPEATER);
    }

    public boolean shouldPrioritize(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING).getOpposite();
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        return isRelay(blockState2) && blockState2.getValue(FACING) != direction;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        boolean powered = blockState.getValue(POWERED);
        int power = blockState.getValue(SIGNAL);
        int inputPower = this.getInputSignal(serverLevel, blockPos, blockState);
        if (powered && inputPower <= 0) {
            serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(POWERED, false).setValue(SIGNAL, 0));
        } else if (!powered || power != inputPower) {
            serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(POWERED, true).setValue(SIGNAL, inputPower));

            if (inputPower <= 0) {
                serverLevel.scheduleTick(blockPos, blockState.getBlock(), this.config.delay, TickPriority.VERY_HIGH);
            }
        }
    }

    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState state = level.getBlockState(blockPos2);
        if (isRelay(state)) {
            return state.getValue(SIGNAL);
        } else if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        } else if (state.is(Blocks.REDSTONE_WALL_TORCH) && state.getValue(RedstoneTorchBlock.LIT)) {
            return 15;
        }
        return level.getSignal(blockPos2, direction);
    }

    @Override
    public BlockState modifyPolymerBlockState(BlockState original, BlockState blockState) {
        return blockState.setValue(SIGNAL, 0);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, Level level, BlockPos pos, BlockState blockState2, boolean movedByPiston) {
        if (!movedByPiston)
            this.updateNeighborsInFront(level, pos, state);
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        level.neighborChanged(blockPos2, blockState.getBlock(), blockPos);
        level.updateNeighborsAtExceptFromFacing(blockPos2, blockState.getBlock(), direction);
    }

    @Override
    @NotNull
    public Repeater.Config getConfig() {
        return this.config;
    }

    public static class Config {
        /**
         * delay in ticks
         */
        public int delay = 0;

        /**
         * power loss during "transfer"
         */
        public int loss = 0;
    }
}