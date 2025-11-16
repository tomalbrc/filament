package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemBehaviour<T> extends Behaviour<T> {
    default void init(Item item, BehaviourHolder behaviourHolder) {
    }

    default void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> consumer, TooltipFlag tooltipFlag) {
    }

    default InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    default Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.empty();
    }

    default InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    default void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
    }

    default void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
    }

    default boolean useOnRelease(ItemStack itemStack) {
        return false;
    }

    default void modifyPolymerItemStack(Map<String, ResourceLocation> models, ItemStack original, ItemStack replacement, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
    }

    default boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        return false;
    }

    default boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return true;
    }

    default void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {}

    default float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        return 0.f;
    }

    default @Nullable DamageSource getDamageSource(LivingEntity livingEntity) {
        return null;
    }

    default EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.MAINHAND;
    }

    default int modifyPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player, int color) {
        return -1;
    }

    default int modifyPolymerCustomModelData(Map<String, PolymerModelData> modelData, ItemStack itemStack, @Nullable ServerPlayer player) {
        return -1;
    }
}
