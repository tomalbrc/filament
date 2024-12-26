package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Execute implements ItemBehaviour<Execute.Config> {
    private final Config config;

    public Execute(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Execute.Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult use(Item item, Level level, Player user, InteractionHand hand) {
        if (this.config.command != null && user.getServer() != null) {
            user.getServer().getCommands().performPrefixedCommand(user.createCommandSourceStackForNameResolution((ServerLevel) user.level()).withMaximumPermission(4).withEntity(user).withPosition(user.position()).withRotation(user.getRotationVector()).withLevel((ServerLevel) user.level()), this.config.command);

            user.awardStat(Stats.ITEM_USED.get(item));

            if (this.config.sound != null) {
                var sound = this.config.sound;
                level.playSound(null, user, BuiltInRegistries.SOUND_EVENT.get(sound).orElseThrow().value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                user.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        return ItemBehaviour.super.use(item, level, user, hand);
    }

    public static class Config {
        public boolean consumes;

        public String command;

        public ResourceLocation sound;
    }
}
