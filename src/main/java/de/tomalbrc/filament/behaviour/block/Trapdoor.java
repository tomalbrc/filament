package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.FilamentBlockResourceUtils;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Trapdoor implements BlockBehaviour<Trapdoor.Config>, SimpleWaterloggedBlock {
    private final Config config;

    public Trapdoor(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> Optional.of(blockState.getValue(BlockStateProperties.OPEN));
            case WATER -> Optional.of(blockState.getValue(BlockStateProperties.WATERLOGGED));
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!this.config.canOpenByHand) {
            return InteractionResult.PASS;
        }
        this.toggle(blockState, level, blockPos, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (explosion.canTriggerBlocks() && this.config.canOpenByWindCharge && !blockState.getValue(BlockStateProperties.POWERED)) {
            this.toggle(blockState, level, blockPos, null);
        }
    }

    private void toggle(BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player) {
        BlockState blockState2 = blockState.cycle(BlockStateProperties.OPEN);
        level.setBlock(blockPos, blockState2, Block.UPDATE_CLIENTS);
        if (blockState2.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        this.playSound(player, level, blockPos, blockState2.getValue(BlockStateProperties.OPEN));
    }

    protected void playSound(@Nullable Player player, Level level, BlockPos blockPos, boolean open) {
        level.playSound(player, blockPos, SoundEvent.createVariableRangeEvent(open ? this.config.openSound : this.config.closeSound), SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, Orientation orientation, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (bl2 != blockState.getValue(BlockStateProperties.POWERED)) {
            if (blockState.getValue(BlockStateProperties.OPEN) != bl2) {
                blockState = blockState.setValue(BlockStateProperties.OPEN, bl2);
                this.playSound(null, level, blockPos, bl2);
            }
            level.setBlock(blockPos, blockState.setValue(BlockStateProperties.POWERED, bl2), Block.UPDATE_CLIENTS);
            if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
                level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockState self, BlockPlaceContext blockPlaceContext) {
        BlockState blockState = self.getBlock().defaultBlockState();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        Direction direction = blockPlaceContext.getClickedFace();
        blockState = blockPlaceContext.replacingClickedOnBlock() || !direction.getAxis().isHorizontal() ? blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP) : blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, direction).setValue(BlockStateProperties.HALF, blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
        if (blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())) {
            blockState = blockState.setValue(BlockStateProperties.OPEN, true).setValue(BlockStateProperties.POWERED, true);
        }
        return blockState.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN, BlockStateProperties.HALF, BlockStateProperties.POWERED, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return BlockBehaviour.super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).setValue(BlockStateProperties.OPEN, false).setValue(BlockStateProperties.HALF, Half.BOTTOM);
    }

    @Override
    public boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> data) {
        for (Map.Entry<String, PolymerBlockModel> entry : data.blockResource().models().entrySet()) {
            PolymerBlockModel blockModel = entry.getValue();

            BlockStateParser.BlockResult parsed;
            String str = String.format("%s[%s]", data.id(), entry.getKey());
            try {
                parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid BlockState value: " + str);
            }

            map.put(parsed.blockState(), BlockData.BlockStateMeta.of(dryState(parsed, blockModel, data.virtual()), blockModel));
            map.put(parsed.blockState().setValue(BlockStateProperties.WATERLOGGED, true), BlockData.BlockStateMeta.of(wetState(parsed, blockModel, data.virtual()), blockModel));
        }

        return true;
    }

    private BlockState dryState(BlockStateParser.BlockResult parsed, PolymerBlockModel blockModel, boolean virtual) {
        BlockState requestedState = null;
        if (parsed.blockState().getValue(BlockStateProperties.HALF) == Half.TOP && !parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.TOP_TRAPDOOR, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HALF) == Half.BOTTOM && !parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.BOTTOM_TRAPDOOR, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.NORTH_TRAPDOOR, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.EAST_TRAPDOOR, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.SOUTH_TRAPDOOR, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.WEST_TRAPDOOR, blockModel, virtual);
        }

        return requestedState;
    }

    private BlockState wetState(BlockStateParser.BlockResult parsed, PolymerBlockModel blockModel, boolean virtual) {
        BlockState requestedState = null;
        if (parsed.blockState().getValue(BlockStateProperties.HALF) == Half.TOP && !parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.TOP_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HALF) == Half.BOTTOM && !parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.BOTTOM_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.NORTH_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.EAST_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.SOUTH_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && parsed.blockState().getValue(BlockStateProperties.OPEN)) {
            requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.WEST_TRAPDOOR_WATERLOGGED, blockModel, virtual);
        }

        return requestedState;
    }

    @Override
    public BlockState modifyPolymerBlockState(BlockState original, BlockState blockState) {
        return blockState.setValue(BlockStateProperties.POWERED, false);
    }

    public static class Config {
        public boolean canOpenByWindCharge = true;
        public boolean canOpenByHand = true;
        public ResourceLocation openSound = SoundEvents.WOODEN_TRAPDOOR_OPEN.location();
        public ResourceLocation closeSound = SoundEvents.WOODEN_TRAPDOOR_CLOSE.location();
    }
}