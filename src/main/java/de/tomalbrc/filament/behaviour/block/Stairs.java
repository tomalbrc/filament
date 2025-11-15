package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.block.SimpleBlock;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class Stairs implements BlockBehaviour<Stairs.Config>, SimpleWaterloggedBlock {
  private final Config config;

  public Stairs(Config config) {
    this.config = config;
  }

  @Override
  @NotNull
  public Stairs.Config getConfig() {
    return this.config;
  }

  @Override
  public BlockState modifyDefaultState(BlockState blockState) {
    return blockState.setValue(BlockStateProperties.FACING, Direction.NORTH).setValue(BlockStateProperties.HALF, Half.BOTTOM).setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT).setValue(BlockStateProperties.WATERLOGGED, false);
  }

  @Override
  public Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
    return Optional.of(false);
  }

  @Override
  public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(BlockStateProperties.FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE, BlockStateProperties.WATERLOGGED);
  }
  private static boolean canTakeShape(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
    BlockState blockState = level.getBlockState(pos.relative(face));
    return !isStairs(blockState) || blockState.getValue(BlockStateProperties.FACING) != state.getValue(BlockStateProperties.FACING) || blockState.getValue(BlockStateProperties.HALF) != state.getValue(BlockStateProperties.HALF);
  }

  public static boolean isStairs(BlockState state) {
    if(state.getBlock() instanceof SimpleBlock) {
      return ((SimpleBlock) state.getBlock()).getBehaviours().iterator().next().getKey().id().toString().equals("filament:stairs");
    } else {
      return false;
    }
  }

  private static StairsShape getStairsShape(BlockState state, BlockGetter level, BlockPos pos) {
    Direction direction = state.getValue(BlockStateProperties.FACING);
    BlockState blockState = level.getBlockState(pos.relative(direction));
    if (isStairs(blockState) && state.getValue(BlockStateProperties.HALF) == blockState.getValue(BlockStateProperties.HALF)) {
      Direction direction2 = blockState.getValue(BlockStateProperties.FACING);
      if (direction2.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis() && canTakeShape(state, level, pos, direction2.getOpposite())) {
        if (direction2 == direction.getCounterClockWise()) {
          return StairsShape.OUTER_LEFT;
        }

        return StairsShape.OUTER_RIGHT;
      }
    }

    BlockState blockState2 = level.getBlockState(pos.relative(direction.getOpposite()));
    if (isStairs(blockState2) && state.getValue(BlockStateProperties.HALF) == blockState2.getValue(BlockStateProperties.HALF)) {
      Direction direction3 = blockState2.getValue(BlockStateProperties.FACING);
      if (direction3.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis() && canTakeShape(state, level, pos, direction3)) {
        if (direction3 == direction.getCounterClockWise()) {
          return StairsShape.INNER_LEFT;
        }

        return StairsShape.INNER_RIGHT;
      }
    }

    return StairsShape.STRAIGHT;
  }

  @Override
  public BlockState getStateForPlacement(BlockState prevBlockState, BlockPlaceContext context) {
    Direction direction = context.getClickedFace();
    BlockPos blockPos = context.getClickedPos();
    FluidState fluidState = context.getLevel().getFluidState(blockPos);
    BlockState blockState = this.modifyDefaultState(prevBlockState).setValue(BlockStateProperties.FACING, context.getHorizontalDirection()).setValue(BlockStateProperties.HALF, direction != Direction.DOWN && (direction == Direction.UP || !(context.getClickLocation().y - (double)blockPos.getY() > (double)0.5F)) ? Half.BOTTOM : Half.TOP).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    return blockState.setValue(BlockStateProperties.STAIRS_SHAPE, getStairsShape(blockState, context.getLevel(), blockPos));

  }

  @Override
  public Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
    return Optional.of(false);
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
    return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
  }

  @Override
  public boolean canPlaceLiquid(@Nullable LivingEntity livingEntity, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
    return SimpleWaterloggedBlock.super.canPlaceLiquid(livingEntity, blockGetter, blockPos, blockState, fluid);
  }

  @Override
  public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
    if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
      scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
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

  public static BlockModelType getStairs(Direction direction, Half blockHalf, StairsShape shape, boolean waterlogged) {
    if (direction.getAxis().isVertical()) {
      throw new IllegalArgumentException("Only horizontal directions are supported!");
    }

    var self = new StringBuilder();
    self.append("STAIRS_").append(direction.name())
            .append("_").append(blockHalf.name())
            .append("_").append(shape.name());


    if (waterlogged) {
      self.append("_WATERLOGGED");
    }

    // Bit ugly, but works
    return BlockModelType.valueOf(self.toString());
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

      BlockState state = parsed.blockState();
      BlockModelType stairState = getStairs(
              state.getValue(BlockStateProperties.FACING),
              state.getValue(BlockStateProperties.HALF),
              state.getValue(BlockStateProperties.STAIRS_SHAPE),
              state.getValue(BlockStateProperties.WATERLOGGED)
      );

      BlockState requestedState = FilamentBlockResourceUtils.requestBlock(stairState, blockModel, data.virtual());
      map.put(parsed.blockState(), BlockData.BlockStateMeta.of(requestedState, blockModel));
    }

    return true;
  }

  public static class Config {
  }
}
