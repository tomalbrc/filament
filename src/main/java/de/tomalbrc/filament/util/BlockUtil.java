package de.tomalbrc.filament.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SoundType;

public class BlockUtil {
    public static void handleBoneMealEffects(ServerLevel level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getCenter().x, blockPos.getCenter().y, blockPos.getCenter().z, 15, 0.25, 0.25, 0.25, 0.15);
    }

    public static void handleBlockPlaceEffects(ServerPlayer player, InteractionHand hand, BlockPos pos, SoundType type) {
        player.swing(hand, true);
        playBlockPlaceSound(player, pos, type);
    }

    public static void playBlockPlaceSound(ServerPlayer player, BlockPos pos, SoundType type) {
        player.connection.send(new ClientboundSoundPacket(
                Holder.direct(type.getPlaceSound()),
                SoundSource.BLOCKS,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                (type.getVolume() + 1.0F) / 2.0F,
                type.getPitch() * 0.8F,
                player.level().getRandom().nextLong()
        ));
    }
}
