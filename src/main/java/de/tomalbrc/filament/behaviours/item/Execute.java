package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Execute implements ItemBehaviour<Execute.ExecuteConfig> {
    private final ExecuteConfig config;

    public Execute(ExecuteConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ExecuteConfig getConfig() {
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player user, InteractionHand hand) {
        if (this.config.command != null) {
            user.getServer().getCommands().performPrefixedCommand(user.createCommandSourceStack(), this.config.command);

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

    public static class ExecuteConfig {
        public boolean consumes;

        public String command;

        public ResourceLocation sound;
    }
}
