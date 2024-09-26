package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class Instrument implements ItemBehaviour<Instrument.InstrumentConfig> {
    private final InstrumentConfig config;

    public Instrument(InstrumentConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public InstrumentConfig getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult use(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        player.startUsingItem(interactionHand);
        play(level, player, config);
        if (config.useDuration > 0) player.getCooldowns().addCooldown(itemStack, config.useDuration);

        player.awardStat(Stats.ITEM_USED.get(item));

        return InteractionResult.CONSUME;
    }

    private static void play(Level level, Player player, Instrument.InstrumentConfig instrument) {
        float f = instrument.range / 25.0F;
        level.playSound(null, player, SoundEvent.createVariableRangeEvent(instrument.sound), SoundSource.RECORDS, f, 1.0F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }

    public static class InstrumentConfig {
        public ResourceLocation sound = null;

        public int range = 0;

        public int useDuration = 0;
    }
}
