package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.decoration.block.entity.DecorationLike;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Defines the behavioral contract for decorations
 * <p>
 * This interface provides the hooks needed to customize their placement, interaction, etc
 *
 * @param <T> the type of the configuration object associated with this behaviour
 */
public interface DecorationBehaviour<T> extends Behaviour<T> {
    /**
     * Called after a decoration was placed.
     * <p>
     * Use this hook to initialize any resources or state that the decoration needs
     * when placed in the world.
     * DecorationBehaviours are attached to the decoration. Each decoration owns an instance.
     * This is different compared to items or blocks, where it is attached to the "global" block/item object.
     *
     * @param decoration the decoration this behaviour is attached to
     */
    default void init(DecorationLike decoration) {
    }

    /**
     * Creates the {@link FilamentDecorationHolder} responsible for managing the
     * client-side visual representation of this decoration.
     * <p>
     * The holder is typically responsible for spawning and updating the
     * display and interaction entities that represent the decoration in
     * the world.
     * Returning {@code null} indicates that the default holder
     * creation logic should be used.
     *
     * @param decoration the decoration this behaviour is attached to
     * @return a new holder instance, or {@code null} to use the default
     */
    default FilamentDecorationHolder createHolder(DecorationLike decoration) {
        return null;
    }

    /**
     * Called when a {@link FilamentDecorationHolder} is attached to the
     * world
     *
     * @param decoration the decoration
     * @param holder     the holder that was attached
     */
    default void onHolderAttach(DecorationLike decoration, FilamentDecorationHolder holder) {
    }

    /**
     * Called when a player interacts (right-clicks) the decoration.
     * <p>
     * The interaction location is provided as a precise Vec3, allowing
     * behaviours to determine where the decoration was clicked, e.g. for shelves.
     * Returning {@link InteractionResult#PASS}
     * allows other behaviours or the default logic to handle the interaction.
     *
     * @param player     the player interacting with the decoration
     * @param hand       the hand used for the interaction
     * @param location   the exact point of interaction in world space
     * @param decoration the decoration being interacted with
     * @return the result of the interaction
     */
    default InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationLike decoration) {
        return InteractionResult.PASS;
    }

    /**
     * Reads custom data from the decoration's saved storage.
     * <p>
     * This method is called when the decoration is loaded.
     * Implementations should read any behaviour-specific fields they
     * previously wrote in {@link #write(ValueOutput, DecorationLike)}.
     *
     * @param output     the input value to read from
     * @param decoration the decoration
     */
    default void read(ValueInput output, DecorationLike decoration) {
    }

    /**
     * Writes custom data to the decoration's saved storage.
     * <p>
     * This method is called when the decoration is saved.
     * Implementations should write any behaviour-specific state that needs to
     * persist across world reloads.
     *
     * @param input      the output value to write to
     * @param decoration the decoration
     */
    default void write(ValueOutput input, DecorationLike decoration) {
    }

    /**
     * Called when the decoration is destroyed.
     * <p>
     * Implementations can use this to drop custom items, play effects, or
     * clean up any resources they allocated.
     *
     * @param decoration the decoration that was destroyed
     * @param dropItem   whether the decoration should drop its item form
     */
    default void destroy(DecorationLike decoration, boolean dropItem) {
    }

    /**
     * Allows modification of the item stack that is dropped when the decoration
     * is broken.
     * <p>
     * This can be used to add custom components or
     * otherwise alter the drop.
     *
     * @param decoration the decoration
     * @param itemStack  the default drop item stack
     */
    default void modifyDrop(DecorationLike decoration, ItemStack itemStack) {
    }

    /**
     * Allows changing the visual item stack used to represent the decoration on
     * the client for simple default ElementHolders.
     * <p>
     * This is useful when the decoration's appearance should differ from the
     * item stack that was used to place it (e.g. based on block state, contents,
     * or other conditions).
     *
     * @param decoration the decoration
     * @param adjusted   the current adjusted item stack
     * @param blockState the block state of the decoration, or {@code null} for entity decorations
     * @return the item stack to use for the client-side visual
     */
    // Allows to change the visual item stack
    default ItemStack visualItemStack(DecorationLike decoration, ItemStack adjusted, BlockState blockState) {
        return adjusted;
    }

    /**
     * Called to update the decoration's block state when a neighbouring block
     * changes.
     * <p>
     * This mirrors the block shape update logic, allowing decorations to react
     * to changes in their surroundings (e.g. a shelf decoration that needs to
     * re-evaluate its shape when a support block is removed).
     * <p>
     * Block-only hook. Entity-backed decorations never call it!
     *
     * @param decoration          the decoration
     * @param blockState          the current block state
     * @param levelReader         the level reader
     * @param scheduledTickAccess access to scheduled ticks
     * @param blockPos            the position of the decoration
     * @param direction           the direction of the neighbour that changed
     * @param blockPos2           the position of the neighbour
     * @param blockState2         the new block state of the neighbour
     * @param randomSource        the random source
     * @return the updated block state
     */
    default BlockState updateShape(DecorationLike decoration, BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return blockState;
    }

    /**
     * Returns the item stack that should be given when the player uses the
     * "pick block" function on this decoration.
     * <p>
     * This allows the decoration to return a custom item instead of the default block item.
     * <p>
     * Block-only hook. Entity-backed decorations never call it!
     *
     * @param stack       the default clone item stack
     * @param levelReader the level reader
     * @param blockPos    the position of the decoration
     * @param blockState  the block state
     * @param includeData whether to include extra data (e.g. block entity NBT)
     * @return the clone item stack
     */
    default ItemStack getCloneItemStack(ItemStack stack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return stack;
    }

    /**
     * Applies implicit data components from the item.
     *
     * @param decoration          the decoration
     * @param dataComponentGetter the component getter to apply data to
     */
    default void applyImplicitComponents(DecorationLike decoration, DataComponentGetter dataComponentGetter) {}

    /**
     * This is the counterpart to {@link #applyImplicitComponents(DecorationLike, DataComponentGetter)}.
     * Implementations should add any components that are implicitly derived from
     * the decoration's state.
     *
     * @param decoration the decoration
     * @param builder    the component map builder to add components to
     */
    default void collectImplicitComponents(DecorationLike decoration, DataComponentMap.Builder builder) {}

    /**
     * Removes components from the decoration's serialized data.
     * <p>
     * This is used during serialization to strip out components that should not
     * be persisted (e.g. temporary or derived data).
     *
     * @param decoration  the decoration
     * @param valueOutput the output value to remove components from
     */
    default void removeComponentsFromTag(DecorationLike decoration, ValueOutput valueOutput) {}

    /**
     * Called after the decoration has been broken by a player.
     * <p>
     * This is distinct from {@link #destroy(DecorationLike, boolean)},
     * which is called during the destruction process. {@code postBreak} is
     * called after the decoration has been fully removed.
     *
     * @param decoration the decoration that was broken
     * @param blockPos   the position where the decoration was
     * @param player     the player who broke the decoration
     */
    default void postBreak(DecorationLike decoration, BlockPos blockPos, Player player) {
    }
}
