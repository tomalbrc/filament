package de.tomalbrc.filament.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockUtil {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);
    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;

    public static void handleBoneMealEffects(ServerLevel level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getCenter().x, blockPos.getCenter().y, blockPos.getCenter().z, 15, 0.25, 0.25, 0.25, 0.15);
    }

    public static void handleBlockPlaceEffects(ServerPlayer player, InteractionHand hand, BlockPos pos, SoundType type) {
        player.swing(hand, true);
    }

    public static void playBreakSound(Level level, BlockPos blockPos, BlockState blockState) {
        SoundEvent breakSound = blockState.getSoundType().getBreakSound();
        level.playSound(null, blockPos,  breakSound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
