package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Execute implements ItemBehaviour<Execute.Config>, BlockBehaviour<Execute.Config> {
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
        var cmds = commands();

        if (cmds != null && user instanceof ServerPlayer serverPlayer) {
            runCommandItem(serverPlayer, item, hand);
            return InteractionResult.CONSUME;
        }

        return ItemBehaviour.super.use(item, level, user, hand);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            runCommandBlock(serverPlayer, blockPos);
            return InteractionResult.SUCCESS_SERVER;
        }
        return BlockBehaviour.super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }

    @Override
    public @Nullable InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            runCommandBlock(serverPlayer, blockPos);
            return InteractionResult.SUCCESS_SERVER;
        }
        return BlockBehaviour.super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
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

    public void runCommandBlock(ServerPlayer user, BlockPos blockPos) {
        var cmds = commands();
        if (cmds != null) {
            var pos = getConfig().atBlock ? blockPos.getCenter() : null;
            if (getConfig().console) {
                ExecuteUtil.asConsole(user, pos, cmds.toArray(new String[0]));
            }
            else {
                ExecuteUtil.asPlayer(user, pos, cmds.toArray(new String[0]));
            }

            if (config.console) {
                ExecuteUtil.asConsole(user, null, cmds.toArray(new String[0]));
            } else {
                ExecuteUtil.asPlayer(user, config.atBlock ? blockPos.getCenter() : null, cmds.toArray(new String[0]));
            }

            if (this.config.sound != null) {
                var sound = this.config.sound;
                user.level().playSound(null, user, BuiltInRegistries.SOUND_EVENT.getValue(sound), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                user.level().destroyBlock(blockPos, config.dropBlock, user);
            }
        }
    }

    private List<String> commands() {
        return config.commands == null ? this.config.command == null ? null : List.of(this.config.command) : config.commands;
    }

    public static class Config {
        public boolean consumes;

        public String command;
        public List<String> commands;

        public boolean atBlock = false;

        public boolean dropBlock = false;

        public ResourceLocation sound;
        public boolean console = false;
    }
}
