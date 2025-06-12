package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface BlockBehaviour<T> extends Behaviour<T> {
    /**
     * Called after the block and item were registered
     * @param item Item of the block
     * @param block The block
     * @param behaviourHolder filament behaviour holder (usually the block)
     */
    default void init(Item item, Block block, BehaviourHolder behaviourHolder) {

    }

    /**
     * Allows to modify the blockstate to block-model map used by filament/polymer for the client-side representation of a blockstate (might be filtered)
     * @param map current default blockstate to model map
     * @param blockData block data
     * @return true when the state-map was modified
     */
    default boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, BlockData<? extends BlockProperties> blockData) {
        return false;
    }

    /**
     * Allows to modify the default blockstate of the block holding this behaviour
     * @param blockState the current default blockstate
     * @return the new default blockstate
     */
    default BlockState modifyDefaultState(BlockState blockState) {
        return blockState;
    }

    /**
     * Allows to modify the blocks' vanilla properties
     * @param properties the current properties
     * @return the modified block properties
     */
    default net.minecraft.world.level.block.state.BlockBehaviour.Properties modifyBlockProperties(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        return properties;
    }

    /**
     * Allows to add block-state-properties to the block holding this behaviour
     * @param builder state-definition builder
     * @return flag whether something was added
     */
    default boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        return false;
    }

    /**
     *
     * @param blockState the blockstate
     * @return optional wether to use the blockstates shape for light occlusion
     */
    default Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
        return Optional.empty();
    }

    /**
     * Allows to modify the blockstate for a given placement, before being placed.
     * @param blockState default blockstate (or modified one by a different behaviour)
     * @param blockPlaceContext context
     * @return new blockstate for placement
     */
    default BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockState;
    }

    /**
     * Whether the block can be replaced like grass when placing another block in its place
     * @param blockState blockstate to replace
     * @param blockPlaceContext context
     * @return optional flag
     */
    default Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return Optional.empty();
    }

    /**
     * Shape update
     */
    default BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return blockState;
    }

    /**
     *
     */
    default void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, Orientation orientation, boolean bl) {

    }

    /**
     *
     */
    default void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
    }

    /**
     *
     */
    default Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return Optional.empty();
    }

    /**
     *
     */
    @Nullable
    default FluidState getFluidState(BlockState blockState) {
        return null;
    }

    /**
     *
     */
    default void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
    }

    /**
     *
     */
    default boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    /**
     *
     */
    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return null;
    }

    /**
     *
     */
    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return null;
    }

    /**
     *
     */
    default boolean isSignalSource(BlockState blockState) {
        return false;
    }

    /**
     *
     */
    default int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    /**
     *
     */
    default int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    /**
     *
     */
    default void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    /**
     *
     */
    default void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
    }

    /**
     * Called on removal to update neighbours
     */
    default void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean movedByPiston) {
    }

    /**
     *
     */
    default void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
    }

    /**
     *
     */
    default boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    /**
     *
     */
    default boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    /**
     *
     */
    default void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    /**
     * Tick
     * @param blockState state of ticking block
     * @param serverLevel level
     * @param blockPos position
     * @param randomSource random source
     */
    default void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    /**
     * In some cases your block behaviour may need block states that visually look the same on clients.
     * This method allows to change block state properties before being passed on to getPolymerBlockState
     */
    default BlockState modifyPolymerBlockState(BlockState originalBlockState, BlockState blockState) {
        return blockState;
    }

    /**
     * Allows to change the clone item-stack of the block
     *
     * @param itemStack The item of the block
     * @param levelReader The level(-reader)
     * @param blockPos Position of the block in world
     * @param blockState State of the picked block
     * @return The clone (pick) itemstack of the block
     */
    default ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return null;
    }

    /**
     * Called when interacted with, with empty hands
     *
     * @param blockState State of the block interacted with
     * @param level World of the player/block
     * @param blockPos position of the block in the world
     * @param player Player that interacted with the block
     * @param blockHitResult Block-hit information
     * @return The result of the interaction with this block
     */
    default InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return InteractionResult.PASS;
    }

    /**
     * Called when interacted with, with an item in hand.
     *
     * @param itemStack Item that was held by the player / used for the interaction
     * @param blockState State of the block that was interacted with
     * @param level level
     * @param blockPos block pos of interaction
     * @param player Player that interacted with the block
     * @param interactionHand interaction hand
     * @param blockHitResult Block-hit information
     * @return The result of the interaction with this block with the item
     */
    @Nullable
    default InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return null;
    }

    /**
     * Called when a player "attacks" the block
     */
    default void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
    }

    /**
     * Called when a projectile hits the block.
     *
     * @param level level
     * @param blockState state of the hit block
     * @param blockHitResult Block-hit information
     * @param projectile Projectile entity
     */
    default void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
    }

    /**
     * Seed for blockstate at blockpos
     */
    default Optional<Long> getSeed(BlockState blockState, BlockPos blockPos) {
        return Optional.empty();
    }

    /**
     * Damage source that is used for fall damage
     * @param entity damaged entity
     * @return the source of damage
     */
    @Nullable
    default DamageSource getFallDamageSource(Entity entity) {
        return null;
    }

    /**
     * Called when a falling block lands
     */
    default void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
    }

    /**
     * Callback on broken after fall
     */
    default void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    }

    /**
     * Called when a block was exploded. Used for tnt behaviour for example to ignite the block
     */
    default void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
    }

    default Optional<Boolean> hasAnalogOutputSignal(BlockState blockState) {
        return Optional.empty();
    }

    default int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return 0;
    }

    default void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
    }

    default int getLightBlock(BlockState state) {
        return -1;
    }

    default VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return null;
    }
}