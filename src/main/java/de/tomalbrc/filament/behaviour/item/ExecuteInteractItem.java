package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExecuteInteractItem implements ItemBehaviour<ExecuteInteractItem.Config> {
    private final Config config;

    public ExecuteInteractItem(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ExecuteInteractItem.Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult use(Item item, Level level, Player user, InteractionHand hand) {
        var cmds = commands();

        if (cmds != null && user instanceof ServerPlayer serverPlayer) {
            runCommandItem(serverPlayer, item, hand);
            return InteractionResult.CONSUME;
        }

        return ItemBehaviour.super.use(item, level, user, hand);
    }

    public void runCommandItem(ServerPlayer serverPlayer, Item item, InteractionHand hand) {
        if (serverPlayer.getCooldowns().isOnCooldown(serverPlayer.getItemInHand(hand)))
            return;

        var cmds = commands();
        if (cmds != null) {
            if (config.console) {
                ExecuteUtil.asConsole(serverPlayer, null, cmds.toArray(new String[0]));
            } else {
                ExecuteUtil.asPlayer(serverPlayer, null, cmds.toArray(new String[0]));
            }

            serverPlayer.awardStat(Stats.ITEM_USED.get(item));

            if (this.config.sound != null) {
                var sound = this.config.sound;
                serverPlayer.level().playSound(null, serverPlayer, BuiltInRegistries.SOUND_EVENT.getValue(sound), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                serverPlayer.getItemInHand(hand).shrink(1);
            } else if (this.config.damages) {
                serverPlayer.getItemInHand(hand).hurtAndBreak(1, serverPlayer, hand);
            }
        }
    }

    private List<String> commands() {
        return config.commands == null ? this.config.command == null ? null : List.of(this.config.command) : config.commands;
    }

    public static class Config {
        public boolean consumes;
        public boolean damages;

        public String command;
        public List<String> commands;

        public Identifier sound;
        public boolean console = false;
    }
}
