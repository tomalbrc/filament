package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class Instrument implements ItemBehaviour<Instrument.Config> {
    private final Config config;

    public Instrument(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Instrument.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        player.startUsingItem(interactionHand);
        play(level, player, config);
        if (config.useDuration > 0) player.getCooldowns().addCooldown(itemStack.getItem(), config.useDuration);

        player.awardStat(Stats.ITEM_USED.get(item));

        return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
    }

    private static void play(Level level, Player player, Config instrument) {
        float f = instrument.range / 25.0F;
        level.playSound(null, player, SoundEvent.createVariableRangeEvent(instrument.sound), SoundSource.RECORDS, f, 1.0F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }

    public static class Config {
        public ResourceLocation sound = null;

        public int range = 0;

        public int useDuration = 0;
    }
}
