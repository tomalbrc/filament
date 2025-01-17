package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface BlockBehaviour<T> extends Behaviour<T> {
    default void init(Item item, Block block, BehaviourHolder behaviourHolder) {

    }

    default boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, BlockData blockData) {
        return false;
    }

    default BlockState modifyDefaultState(BlockState blockState) {
        return blockState;
    }

    default boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        return false;
    }

    default Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
        return Optional.empty();
    }

    default BlockState getStateForPlacement(BlockState block, BlockPlaceContext blockPlaceContext) {
        return block;
    }

    default Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return Optional.empty();
    }

    default BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState;
    }

    default void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
    }

    default void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
    }

    default Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return Optional.empty();
    }

    @Nullable
    default FluidState getFluidState(BlockState blockState) {
        return null;
    }

    default void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
    }

    default boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return null;
    }

    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return null;
    }

    default void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
    }

    default boolean isSignalSource(BlockState blockState) {
        return false;
    }

    default int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    default int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    default void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    default void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
    }

    default void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    default boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    default boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    default void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    default void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    default BlockState getCustomPolymerBlockState(Map<BlockState, BlockData.BlockStateMeta> stateMap, BlockState blockState) {
        return null;
    }

    default ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return null;
    }

    default InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return InteractionResult.PASS;
    }

    @Nullable
    default InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return null;
    }

    default void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
    }

    default Optional<Long> getSeed(BlockState blockState, BlockPos blockPos) {
        return Optional.empty();
    }

    @Nullable
    default DamageSource getFallDamageSource(Entity entity) {
        return null;
    }

    default void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
    }

    default void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    }

    default void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {

    }
}