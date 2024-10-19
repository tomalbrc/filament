package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Shield behaviour
 */
public class Shield implements ItemBehaviour<Shield.Config> {
    private final Config config;

    public Shield(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Shield.Config getConfig() {
        return this.config;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
    }

    public Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.of(72000);
    }

    @Override
    public InteractionResult use(Item self, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;
    }

    public static class Config {
    }
}