package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused"})
public interface ItemBehaviour<T> extends Behaviour<T> {
    default void init(Item item, BehaviourHolder behaviourHolder) {
    }

    default void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
    }

    default InteractionResult use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    default Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.empty();
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

    default void modifyPolymerItemStack(Map<String, ResourceLocation> models, ItemStack original, ItemStack replacement, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
    }

    default int modifyPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player, int color) {
        return color;
    }
}
