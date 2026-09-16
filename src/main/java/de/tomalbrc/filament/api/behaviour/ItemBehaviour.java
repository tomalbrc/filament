package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.properties.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Defines the behavioral contract for a custom item.
 * <p>
 * Implementations of this interface provide the logic that control how an item
 * behaves when used, held, or interacted with. The interface mirrors many of the
 * hooks present in vanilla's {@link Item} class but is designed to be composed
 *
 * @param <T> the type of the configuration object associated with this behaviour
 */
public interface ItemBehaviour<T> extends Behaviour<T> {
    /**
     * Called after the item has been registered.
     *
     * @param item            the item instance
     * @param behaviourHolder the holder that owns this behaviour (usually the item itself)
     */
    default void init(Item item, BehaviourHolder behaviourHolder) { // TODO: pass FilamentItem?
    }

    /**
     * Appends additional lines to the item's tooltip.
     *
     * @param itemStack      the item stack being described
     * @param tooltipContext the tooltip context
     * @param tooltipDisplay controls which tooltip sections are shown
     * @param consumer       the consumer that accepts the tooltip components
     * @param tooltipFlag    flags describing the tooltip request (e.g. advanced tooltips)
     */
    default void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
    }

    /**
     * Called when the item is used (right-clicked) while held.
     *
     * @param item            the item being used
     * @param level           the level in which the use occurs
     * @param player          the player using the item
     * @param interactionHand the hand used to activate the item
     * @return the result of the interaction
     */
    default InteractionResult use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    /**
     * Returns the number of ticks the item can be used for.
     *
     * @param itemStack    the item stack
     * @param livingEntity the entity using the item
     * @return an {@link Optional} containing the use duration in ticks, or
     *         {@link Optional#empty()} if the item has no use duration
     */
    default Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.empty();
    }

    /**
     * Returns the animation played while the item is being used.
     *
     * @param itemStack the item stack
     * @return the use animation to play
     */
    default ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.NONE;
    }

    /**
     * Called when the item is used on a block.
     *
     * @param useOnContext the context of the use-on-block interaction
     * @return the result of the interaction
     */
    default InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    /**
     * Called every tick while the item is being used.
     *
     * @param level        the level
     * @param livingEntity the entity using the item
     * @param itemStack    the item stack
     * @param i            the number of ticks the item has been used
     */
    default void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
    }

    /**
     * Called when the entity stops using the item (e.g. releases the right-click button).
     *
     * @param itemStack    the item stack
     * @param level        the level
     * @param livingEntity the entity that was using the item
     * @param useDuration  the total number of ticks the item was used for
     * @return {@code true} if the release should be handled as a successful use
     */
    default boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        return false;
    }

    /**
     * Determines whether the item should trigger its use-on-release logic when released.
     *
     * @param itemStack the item stack
     * @return {@code true} if release triggers use-on-release, {@code false} otherwise
     */
    default boolean useOnRelease(ItemStack itemStack) {
        return false;
    }

    /**
     * Allows modification of the client-side Polymer representation of the item stack.
     *
     * @param models      a map of model names to {@link Identifier}s from the ItemResource
     * @param original    the original server-side item stack
     * @param replacement the replacement client-side item stack to modify
     * @param tooltipType the tooltip flags
     * @param lookup      the registry lookup provider
     * @param player      the player viewing the item, or {@code null} if not player-specific
     */
    default void modifyPolymerItemStack(Map<String, Identifier> models, ItemStack original, ItemStack replacement, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
    }

    /**
     * Called when the item is used to break a block.
     *
     * @param itemStack    the item stack
     * @param level        the level
     * @param blockState   the blockstate of the broken block
     * @param blockPos     the position of the block
     * @param livingEntity the entity breaking the block
     * @return {@code true} if the block break should be considered handled
     */
    default boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        return false;
    }

    /**
     * Called when the item is used to hurt another entity.
     *
     * @param itemStack    the item stack
     * @param livingEntity the entity being hurt
     * @param livingEntity2 the entity dealing the damage
     */
    default void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
    }

    /**
     * Called after the item has successfully hurt an entity.
     * <p>
     * This is typically used for post-damage effects such as durability loss or
     * special on-hit behaviours.
     *
     * @param itemStack     the item stack
     * @param livingEntity  the entity that was hurt
     * @param livingEntity2 the entity that dealt the damage
     */
    default void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {}

    /**
     * Returns the bonus attack damage granted by this item when attacking the given entity.
     *
     * @param entity       the entity being attacked
     * @param f            the base attack damage
     * @param damageSource the source of the damage
     * @return the bonus attack damage to apply
     */
    default float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        return 0.f;
    }

    /**
     * Returns the damage source used when this item deals damage.
     *
     * @param livingEntity the entity dealing the damage
     * @return the custom damage source, or {@code null} if no custom source is used
     */
    default @Nullable DamageSource getDamageSource(LivingEntity livingEntity) {
        return null;
    }
}
