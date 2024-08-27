package de.tomalbrc.filament.block;

import de.tomalbrc.filament.behaviours.block.Powersource;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class SimpleBlock extends Block implements PolymerTexturedBlock {
    protected final HashMap<String, BlockState> stateMap;
    protected final BlockState breakEventState;
    protected final BlockData blockData;

    public SimpleBlock(BlockBehaviour.Properties properties, BlockData data) {
        super(properties);
        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
        this.blockData = data;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.stateMap.get("default");
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }


    @Override
    public boolean isSignalSource(BlockState blockState) {
        return this.blockData.isPowersource();
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (this.blockData.isPowersource()) {
            assert this.blockData.behaviour() != null;
            Powersource.PowersourceConfig powersource = this.blockData.behaviour().get(Constants.Behaviours.POWERSOURCE);
            return powersource.value;
        } else {
            return 0;
        }
    }
}
