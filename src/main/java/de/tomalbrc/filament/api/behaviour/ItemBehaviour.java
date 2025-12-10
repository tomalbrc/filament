package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
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

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface ItemBehaviour<T> extends Behaviour<T> {
    default void init(Item item, BehaviourHolder behaviourHolder) {
    }

    default void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
    }

    default InteractionResult use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    default Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.empty();
    }

    default ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.NONE;
    }

    default InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    default void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
    }

    default boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        return false;
    }

    default boolean useOnRelease(ItemStack itemStack) {
        return false;
    }

    default void modifyPolymerItemStack(Map<String, Identifier> models, ItemStack original, ItemStack replacement, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
    }

    default boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        return false;
    }

    default void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
    }

    default void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {}

    default float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        return 0.f;
    }

    default @Nullable DamageSource getDamageSource(LivingEntity livingEntity) {
        return null;
    }
}
