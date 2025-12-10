package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExecuteAttackItem implements ItemBehaviour<ExecuteAttackItem.Config> {
    private final Config config;

    public ExecuteAttackItem(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ExecuteAttackItem.Config getConfig() {
        return config;
    }

    public void runCommandItem(ServerPlayer serverPlayer, Item item, InteractionHand hand) {
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
            }
        }
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (config.onEntityAttack && livingEntity2 instanceof ServerPlayer player) {
            runCommandItem(player, itemStack.getItem(), InteractionHand.MAIN_HAND);
        }
    }

    private List<String> commands() {
        return config.commands == null ? this.config.command == null ? null : List.of(this.config.command) : config.commands;
    }

    public static class Config {
        public boolean consumes;

        public String command;
        public List<String> commands;

        public Identifier sound;

        public boolean onEntityAttack = true;
        public boolean console = false;
    }
}
