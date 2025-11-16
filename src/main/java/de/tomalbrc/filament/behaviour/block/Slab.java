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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class Slab implements BlockBehaviour<Slab.Config>, SimpleWaterloggedBlock {
    private final Config config;

    public Slab(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Slab.Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM).setValue(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    public Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
        return Optional.of(blockState.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.SLAB_TYPE, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockState prevBlockState, BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPos);
        if (blockState.is(prevBlockState.getBlock())) {
            return blockState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE).setValue(BlockStateProperties.WATERLOGGED, false);
        }
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
        BlockState blockState2 = prevBlockState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
        Direction direction = blockPlaceContext.getClickedFace();
        if (direction == Direction.DOWN || direction != Direction.UP && blockPlaceContext.getClickLocation().y - (double) blockPos.getY() > 0.5) {
            return blockState2.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
        }
        return blockState2;
    }

    @Override
    public Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        SlabType slabType = blockState.getValue(BlockStateProperties.SLAB_TYPE);
        if (slabType == SlabType.DOUBLE || !itemStack.is(blockState.getBlock().asItem())) {
            return Optional.of(false);
        }
        if (blockPlaceContext.replacingClickedOnBlock()) {
            boolean bl = blockPlaceContext.getClickLocation().y - (double) blockPlaceContext.getClickedPos().getY() > 0.5;
            Direction direction = blockPlaceContext.getClickedFace();
            if (slabType == SlabType.BOTTOM) {
                return Optional.of(direction == Direction.UP || bl && direction.getAxis().isHorizontal());
            }
            return Optional.of(direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal());
        }
        return Optional.of(true);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return null;
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
            return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player livingEntity, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        if (blockState.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
            return SimpleWaterloggedBlock.super.canPlaceLiquid(livingEntity, blockGetter, blockPos, blockState, fluid);
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return blockState;
    }

    @Override
    public Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> Optional.of(false);
            case WATER -> Optional.of(blockState.getFluidState().is(FluidTags.WATER));
        };
    }

    @Override
    public boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> data) {
        for (Map.Entry<String, PolymerBlockModel> entry : data.blockResource().models().entrySet()) {
            PolymerBlockModel blockModel = entry.getValue();

            BlockStateParser.BlockResult parsed;
            String str = String.format("%s[%s]", data.id(), entry.getKey());
            try {
                parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), str, false);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid BlockState value: " + str);
            }

            BlockState requestedState;
            if (parsed.blockState().getValue(SlabBlock.TYPE) == SlabType.TOP) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.TOP_SLAB, blockModel, data.virtual());
            } else if (parsed.blockState().getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.BOTTOM_SLAB, blockModel, data.virtual());
            } else {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, blockModel, data.virtual());
            }

            map.put(parsed.blockState(), BlockData.BlockStateMeta.of(requestedState, blockModel));
        }

        for (Map.Entry<BlockState, BlockData.BlockStateMeta> entry : map.entrySet()) {
            var blockState = entry.getValue().blockState();
            if (blockState.hasProperty(SlabBlock.WATERLOGGED) && !blockState.getValue(SlabBlock.WATERLOGGED) && blockState.hasProperty(SlabBlock.TYPE) && blockState.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                var res = FilamentBlockResourceUtils.requestBlock(blockState.getValue(SlabBlock.TYPE) == SlabType.TOP ? BlockModelType.TOP_SLAB_WATERLOGGED : BlockModelType.BOTTOM_SLAB_WATERLOGGED, entry.getValue().polymerBlockModel(), data.virtual());
                map.put(entry.getKey().setValue(SlabBlock.WATERLOGGED, true), BlockData.BlockStateMeta.of(res, entry.getValue().polymerBlockModel()));
            }
        }
        return true;
    }

    public static class Config {
    }
}