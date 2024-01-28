package de.tomalbrc.filament.item;

import de.tomalbrc.filament.config.data.ItemData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import de.tomalbrc.filament.config.behaviours.item.Instrument;

public class InstrumentItem extends SimpleItem {
    public InstrumentItem(Properties properties, ItemData itemData) {
        super(properties, itemData);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (itemData.isInstrument()) {
            assert itemData.behaviour() != null;
            Instrument instrument = itemData.behaviour().instrument;
            player.startUsingItem(interactionHand);
            play(level, player, instrument);
            if (instrument.useDuration > 0) player.getCooldowns().addCooldown(this, instrument.useDuration);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    private static void play(Level level, Player player, Instrument instrument) {
        float f = instrument.range / 25.0F;
        level.playSound(null, player, SoundEvent.createVariableRangeEvent(instrument.sound), SoundSource.RECORDS, f, 1.0F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }
}
