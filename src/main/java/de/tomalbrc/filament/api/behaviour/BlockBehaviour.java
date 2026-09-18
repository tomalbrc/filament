package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.AbstractBlockData;
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
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Defines the behavioral contract for a custom block
 * <p>
 * Implementations of this interface provide the logic that controls how a block
 * responds to placement, interaction, ticking, redstone signals, and more.
 * The interface mirrors many of the hooks present in vanilla Block class
 *
 * @param <T> the type of the configuration object associated with this behaviour
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface BlockBehaviour<T> extends Behaviour<T> {
    /**
     * Called after the block and its corresponding item have been registered.
     *
     * @param item            the item form of the block
     * @param block           the block instance
     * @param behaviourHolder the holder that owns this behaviour (usually the block itself)
     */
    default void init(Item item, Block block, BehaviourHolder behaviourHolder) {
    }

    /**
     * Allows modification of the blockstate-to-model map used by Filament/Polymer
     * for the client-side representation of a blockstate.
     * <p>
     * The map may be filtered before this method is called. Returning {@code true}
     * signals that the map was modified
     *
     * @param map       the current default blockstate to model mapping
     * @param blockData the data associated with the block
     * @return {@code true} if the state-map was modified, {@code false} otherwise
     */
    default boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> blockData) {
        return false;
    }

    /**
     * Allows modification of the default blockstate of the block holding this behaviour.
     *
     * @param blockState the current default blockstate
     * @return the new default blockstate
     */
    default BlockState modifyDefaultState(BlockState blockState) {
        return blockState;
    }

    /**
     * Allows modification of the block's vanilla properties.
     *
     * @param properties the current properties
     * @return the modified block properties
     */
    default net.minecraft.world.level.block.state.BlockBehaviour.Properties modifyBlockProperties(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        return properties;
    }

    /**
     * Allows adding block-state-properties to the block that holds this behaviour.
     *
     * @param builder the state-definition builder
     */
    default void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    /**
     * Determines whether the block's shape should be used for light occlusion.
     *
     * @param blockState the blockstate to query
     * @return an {@link Optional} containing {@code true} if the shape should be used,
     *         {@code false} if not, or {@link Optional#empty()} to defer to vanilla logic
     */
    default Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
        return Optional.empty();
    }

    /**
     * Allows modification of the blockstate for a given placement, before the block is placed.
     *
     * @param blockState       the default blockstate (or one already modified by another behaviour)
     * @param blockPlaceContext the context of the block placement
     * @return the new blockstate to be used for placement
     */
    default BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockState;
    }

    /**
     * Determines whether the block can be replaced by another block during placement,
     * similar to how grass plants can be replaced.
     *
     * @param blockState        the blockstate to check for replaceability
     * @param blockPlaceContext the context of the block placement
     * @return an {@link Optional} containing {@code true} if replaceable, {@code false} if not,
     *         or {@link Optional#empty()} to defer to vanilla logic
     */
    default Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return Optional.empty();
    }

    /**
     * Called to update the blockstate when a neighbouring block changes.
     *
     * @param blockState         the current blockstate
     * @param levelReader        the level reader
     * @param scheduledTickAccess access to scheduled ticks
     * @param blockPos           the position of this block
     * @param direction          the direction of the neighbour that changed
     * @param blockPos2          the position of the neighbour
     * @param blockState2        the new blockstate of the neighbour
     * @param randomSource       the random source
     * @return the updated blockstate
     */
    default BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return blockState;
    }

    /**
     * Called when a neighbouring block changes.
     *
     * @param blockState the current blockstate
     * @param level      the level
     * @param blockPos   the position of this block
     * @param block      the neighbour block that changed
     * @param orientation the redstone orientation
     * @param bl         whether the change was caused by a redstone update
     */
    default void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, Orientation orientation, boolean bl) {
    }

    /**
     * Called when a player is about to destroy the block.
     *
     * @param level      the level
     * @param blockPos   the position of the block
     * @param blockState the blockstate
     * @param player     the player destroying the block
     */
    default void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
    }

    /**
     * Determines whether the block is pathfindable.
     *
     * @param blockState         the blockstate
     * @param pathComputationType the type of path computation being performed
     * @return an {@link Optional} containing {@code true} if pathfindable, {@code false} if not,
     *         or {@link Optional#empty()} to defer to vanilla logic
     */
    default Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return Optional.empty();
    }

    /**
     * Returns the fluid state of the block, if any.
     *
     * @param blockState the blockstate
     * @return the fluid state, or {@code null} if the block has no fluid
     */
    @Nullable
    default FluidState getFluidState(BlockState blockState) {
        return null;
    }

    /**
     * Called when the block is hit by an explosion.
     *
     * @param blockState the blockstate
     * @param level      the server level
     * @param blockPos   the position of the block
     * @param explosion  the explosion
     * @param biConsumer a consumer for handling dropped items and their positions
     */
    default void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
    }

    /**
     * Determines whether the block should drop items when destroyed by an explosion.
     *
     * @param explosion the explosion
     * @return {@code true} if the block should drop, {@code false} otherwise
     */
    default boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    /**
     * Returns the blockstate rotated by the given rotation.
     *
     * @param blockState the blockstate to rotate
     * @param rotation   the rotation to apply
     * @return the rotated blockstate, or {@code null} if rotation is not handled
     */
    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return null;
    }

    /**
     * Returns the blockstate mirrored by the given mirror.
     *
     * @param blockState the blockstate to mirror
     * @param mirror     the mirror to apply
     * @return the mirrored blockstate, or {@code null} if mirroring is not handled
     */
    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return null;
    }

    /**
     * Determines whether the block is a redstone signal source.
     *
     * @param blockState the blockstate
     * @return {@code true} if the block can emit redstone signals, {@code false} otherwise
     */
    default boolean isSignalSource(BlockState blockState) {
        return false;
    }

    /**
     * Returns the direct redstone signal emitted by this block.
     *
     * @param blockState the blockstate
     * @param blockGetter the block getter
     * @param blockPos   the position of the block
     * @param direction  the direction of the signal
     * @return the direct signal strength (0–15)
     */
    default int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    /**
     * Returns the redstone signal emitted by this block.
     *
     * @param blockState the blockstate
     * @param blockGetter the block getter
     * @param blockPos   the position of the block
     * @param direction  the direction of the signal
     * @return the signal strength (0–15)
     */
    default int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    /**
     * Determines whether a redstone wire should connect to this block from the given direction.
     *
     * @param state     the blockstate
     * @param level     the block getter
     * @param pos       the position of the block
     * @param direction the direction from which the redstone wire is connecting
     * @return {@code true} if the wire should connect, {@code false} otherwise
     */
    default boolean shouldRedstoneWireConnectTo(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @Nullable Direction direction) {
        return false;
    }

    /**
     * Called when the block is placed.
     *
     * @param blockState  the blockstate of the placed block
     * @param level       the level
     * @param blockPos    the position of the block
     * @param blockState2 the previous blockstate
     * @param bl          whether the placement was caused by a player
     */
    default void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    /**
     * Called when the block is placed by a living entity.
     *
     * @param level        the level
     * @param blockPos     the position of the block
     * @param blockState   the blockstate
     * @param livingEntity the entity that placed the block
     * @param itemStack    the item stack used for placement
     */
    default void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
    }

    /**
     * Called after the block is removed to update its neighbours.
     *
     * @param blockState    the blockstate that was removed
     * @param serverLevel   the server level
     * @param blockPos      the position of the block
     * @param movedByPiston whether the removal was caused by a piston
     */
    default void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean movedByPiston) {
    }

    /**
     * Called after the block is broken to spawn additional drops or effects.
     *
     * @param blockState the blockstate
     * @param serverLevel the server level
     * @param blockPos   the position of the block
     * @param itemStack  the item used to break the block
     * @param bl         whether the block was broken by a player
     */
    default void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
    }

    /**
     * Determines whether the block can survive at the given position.
     *
     * @param blockState  the blockstate
     * @param levelReader the level reader
     * @param blockPos    the position of the block
     * @return {@code true} if the block can survive, {@code false} otherwise
     */
    default boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    /**
     * Determines whether the block should receive random ticks.
     *
     * @param blockState the blockstate
     * @return {@code true} if the block should tick randomly, {@code false} otherwise
     */
    default boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    /**
     * Called on a random tick for this block.
     *
     * @param blockState   the blockstate
     * @param serverLevel  the server level
     * @param blockPos     the position of the block
     * @param randomSource the random source
     */
    default void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    /**
     * Called on a scheduled tick for this block.
     *
     * @param blockState   the blockstate
     * @param serverLevel  the server level
     * @param blockPos     the position of the block
     * @param randomSource the random source
     */
    default void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    /**
     * Allows modification of the blockstate before it is passed to
     * {@code getPolymerBlockState}.
     * <p>
     * This is useful when the block needs to display a different visual state
     * on the client while retaining the same logical state on the server.
     *
     * @param originalBlockState the original blockstate
     * @param blockState         the blockstate to be modified
     * @return the modified blockstate for the client
     */
    default BlockState modifyPolymerBlockState(BlockState originalBlockState, BlockState blockState) {
        return blockState;
    }

    /**
     * Allows modification of the clone (pick) item stack for this block.
     *
     * @param itemStack    the default item stack
     * @param levelReader  the level reader
     * @param blockPos     the position of the block
     * @param blockState   the blockstate
     * @param includeData  whether to include block entity data
     * @return the clone item stack
     */
    default ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return itemStack;
    }

    /**
     * Called when a player interacts with the block using an empty hand.
     *
     * @param blockState     the blockstate
     * @param level          the level
     * @param blockPos       the position of the block
     * @param player         the player interacting
     * @param blockHitResult the block hit result
     * @return the result of the interaction
     */
    default InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return InteractionResult.PASS;
    }

    /**
     * Called when a player interacts with the block using an item.
     *
     * @param itemStack       the item stack used for the interaction
     * @param blockState      the blockstate
     * @param level           the level
     * @param blockPos        the position of the block
     * @param player          the player interacting
     * @param interactionHand the hand used for the interaction
     * @param blockHitResult  the block hit result
     * @return the result of the interaction, or {@code null} if not handled
     */
    @Nullable
    default InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return null;
    }

    /**
     * Called when a player attacks the block.
     *
     * @param blockState the blockstate
     * @param level      the level
     * @param blockPos   the position of the block
     * @param player     the player attacking the block
     */
    default void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
    }

    /**
     * Called when a projectile hits the block.
     *
     * @param level          the level
     * @param blockState     the blockstate
     * @param blockHitResult the block hit result
     * @param projectile     the projectile that hit the block
     */
    default void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
    }

    /**
     * Returns a seed for the blockstate at the given position.
     *
     * @param blockState the blockstate
     * @param blockPos   the position of the block
     * @return an {@link Optional} containing the seed, or {@link Optional#empty()} if no seed is provided
     */
    default Optional<Long> getSeed(BlockState blockState, BlockPos blockPos) {
        return Optional.empty();
    }

    /**
     * Returns the damage source used for fall damage on this block.
     *
     * @param entity the entity taking fall damage
     * @return the damage source, or {@code null} if no custom damage source is used
     */
    @Nullable
    default DamageSource getFallDamageSource(Entity entity) {
        return null;
    }

    /**
     * Called when a falling block lands on this block.
     *
     * @param level              the level
     * @param blockPos           the position of the block
     * @param blockState         the blockstate of the falling block
     * @param blockState2        the blockstate of the block that landed
     * @param fallingBlockEntity the falling block entity
     */
    default void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
    }

    /**
     * Called after a falling block is broken.
     *
     * @param level              the level
     * @param blockPos           the position of the block
     * @param fallingBlockEntity the falling block entity
     */
    default void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    }

    /**
     * Called when the block is exploded.
     *
     * @param serverLevel the server level
     * @param blockPos    the position of the block
     * @param explosion   the explosion
     */
    default void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
    }

    /**
     * Determines whether the block has an analog output signal.
     *
     * @param blockState the blockstate
     * @return an {@link Optional} containing {@code true} if the block has an analog output,
     *         {@code false} if not, or {@link Optional#empty()} to defer to vanilla logic
     */
    default Optional<Boolean> hasAnalogOutputSignal(BlockState blockState) {
        return Optional.empty();
    }

    /**
     * Returns the analog output signal strength for this block.
     *
     * @param blockState the block state
     * @param level      the level
     * @param blockPos   the position of the block
     * @param direction  the direction of the signal
     * @return the analog signal strength (0–15)
     */
    default int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return 0;
    }

    /**
     * Called when an entity is inside this block.
     *
     * @param blockState              the block state
     * @param level                   the level
     * @param blockPos                the position of the block
     * @param entity                  the entity inside the block
     * @param insideBlockEffectApplier the applier for inside-block effects
     */
    default void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
    }

    /**
     * Returns the light dampening value for this block.
     *
     * @param state the block state
     * @return the light dampening value, or {@code -1} if not handled
     */
    default int getLightDampening(BlockState state) {
        return -1;
    }

    /**
     * Returns the block support shape for this block.
     *
     * @param state      the block state
     * @param level      the block getter
     * @param pos        the position of the block
     * @return the support shape, or {@code null} if not handled
     */
    default VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return null;
    }

    /**
     * Determines whether entity movement should be updated after falling on this block.
     *
     * @param blockGetter the block getter
     * @param entity      the entity
     * @return {@code true} if movement should be updated, {@code false} otherwise
     */
    default boolean updateEntityMovementAfterFallOn(BlockGetter blockGetter, Entity entity) {
        return false;
    }

    /**
     * Called when an entity falls on this block.
     *
     * @param level      the level
     * @param blockState the blockstate
     * @param blockPos   the position of the block
     * @param entity     the entity
     * @param d          the fall distance
     * @return {@code true} if the fall was handled, {@code false} otherwise
     */
    default boolean fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        return false;
    }

    /**
     * Called when an entity steps on this block.
     *
     * @param level      the level
     * @param blockPos   the position of the block
     * @param blockState the blockstate
     * @param entity     the entity stepping on the block
     */
    default void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        // noop
    }

    /**
     * Determines whether it is possible to respawn in this block.
     *
     * @param state the blockstate
     * @return {@code true} if respawning is possible, {@code false} otherwise
     */
    default boolean isPossibleToRespawnInThis(BlockState state) {
        return false;
    }
}