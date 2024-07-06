package de.tomalbrc.filament.block;

import com.mojang.serialization.MapCodec;
import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.ticks.TickPriority;

import java.util.HashMap;

public class PoweredDirectionBlock extends DirectionalBlock implements PolymerTexturedBlock {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    private final boolean isRelay;
    private int delay = 1;
    private int loss = 0;

    public MapCodec<DirectionBlock> codec() {
        return null;
    }

    public PoweredDirectionBlock(Properties properties, BlockData data) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0).setValue(POWERED, false));

        this.isRelay = data.isRepeater();
        if (this.isRelay && data.behaviour().repeater != null) {
            this.delay = data.behaviour().repeater.delay;
            this.loss = data.behaviour().repeater.loss;
        }

        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction dir = blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite();
        BlockState state = this.defaultBlockState().setValue(FACING, dir);
        int s = this.getInputSignal(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), state);
        state = state.setValue(POWERED, s > 0).setValue(POWER, s);
        this.updateNeighborsOnBack(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), state);
        return state;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.isRelay || !blockState.getValue(POWERED)) {
            return 0;
        } else {
            return blockState.getValue(FACING) == direction ? blockState.getValue(POWER)-this.loss : 0;
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (this.isRelay)
            this.checkTickOnNeighbor(level, blockPos, blockState);
    }

    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        boolean powered = blockState.getValue(POWERED);
        int power = blockState.getValue(POWER);
        int s = this.getInputSignal(level, blockPos, blockState);
        if ((s != power || powered != s > 0) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.shouldPrioritize(level, blockPos, blockState)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (powered) {
                tickPriority = TickPriority.VERY_HIGH;
            }

            level.scheduleTick(blockPos, this, this.delay, tickPriority);
        }
    }

    protected void updateNeighborsOnBack(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        level.neighborChanged(blockPos2, this, blockPos);
        level.updateNeighborsAtExceptFromFacing(blockPos2, this, direction);
    }

    public static boolean isRelay(BlockState blockState) {
        return blockState.getBlock() instanceof PoweredDirectionBlock poweredDirectionBlock && poweredDirectionBlock.isRelay;
    }

    public boolean shouldPrioritize(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING).getOpposite();
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        return isRelay(blockState2) && blockState2.getValue(FACING) != direction;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.isRelay) {
            boolean powered = blockState.getValue(POWERED);
            int power = blockState.getValue(POWER);
            int inputPower = this.getInputSignal(serverLevel, blockPos, blockState);
            if (powered && inputPower <= 0) {
                serverLevel.setBlock(blockPos, blockState.setValue(POWERED, false).setValue(POWER, 0), 3);
            } else if (!powered || power != inputPower) {
                serverLevel.setBlock(blockPos, blockState.setValue(POWERED, true).setValue(POWER, inputPower), 3);

                if (inputPower <= 0) {
                    serverLevel.scheduleTick(blockPos, this, this.delay, TickPriority.VERY_HIGH);
                }
            }

            this.updateNeighborsOnBack(serverLevel, blockPos, blockState);
        }
    }

    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState state = level.getBlockState(blockPos2);
        if (state.getBlock() instanceof PoweredDirectionBlock poweredDirectionBlock && poweredDirectionBlock.isRelay) {
            return state.getValue(POWER);
        } else if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        }
        return level.getSignal(blockPos2, direction);
    }

    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.updateNeighborsOnBack(level, blockPos, blockState);
    }

    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!bl && !blockState.is(blockState2.getBlock())) {
            super.onRemove(blockState, level, blockPos, blockState2, bl);
            this.updateNeighborsOnBack(level, blockPos, blockState);
        }
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return stateMap.get(String.format("%s,powered=%b", state.getValue(FACING).getSerializedName(), state.getValue(POWERED)));
    }
}
