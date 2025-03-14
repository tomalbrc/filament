package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Execute implements ItemBehaviour<Execute.ExecuteConfig>, BlockBehaviour<Execute.ExecuteConfig> {
    private final ExecuteConfig config;

    public Execute(ExecuteConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ExecuteConfig getConfig() {
        return config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player user, InteractionHand hand) {
        var cmds = commands();

        if (cmds != null && user.getServer() != null && user instanceof ServerPlayer serverPlayer) {
            for (String cmd : cmds) {
                user.getServer().getCommands().performPrefixedCommand(
                        serverPlayer.createCommandSourceStack().withSource(user.getServer()).withMaximumPermission(4), cmd);
            }

            user.awardStat(Stats.ITEM_USED.get(item));

            if (this.config.sound != null) {
                var sound = this.config.sound;
                level.playSound(null, user, BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                user.getItemInHand(hand).shrink(1);
            }
            return InteractionResultHolder.consume(user.getItemInHand(hand));
        }

        return ItemBehaviour.super.use(item, level, user, hand);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            runCommandBlock(serverPlayer, blockPos);
            return InteractionResult.SUCCESS;
        }
        return BlockBehaviour.super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }

    @Override
    public @Nullable ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            runCommandBlock(serverPlayer, blockPos);
            return ItemInteractionResult.SUCCESS;
        }
        return BlockBehaviour.super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private void runCommandBlock(ServerPlayer user, BlockPos blockPos) {
        var cmds = commands();
        if (cmds != null && user.getServer() != null) {
            for (String cmd : cmds) {
                var css = user.createCommandSourceStack().withSource(user.getServer()).withMaximumPermission(4);
                if (config.atBlock)
                    css.withPosition(blockPos.getCenter());

                user.getServer().getCommands().performPrefixedCommand(css, cmd);
            }
            if (this.config.sound != null) {
                var sound = this.config.sound;
                user.serverLevel().playSound(null, user, BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                user.serverLevel().destroyBlock(blockPos, config.dropBlock, user);
            }
        }
    }

    private List<String> commands() {
        return config.commands == null ? this.config.command == null ? null : List.of(this.config.command) : config.commands;
    }

    public static class ExecuteConfig {
        public boolean consumes;

        public String command;
        public List<String> commands;

        public boolean atBlock = false;

        public boolean dropBlock = false;

        public ResourceLocation sound;
    }
}
