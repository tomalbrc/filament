package de.tomalbrc.filament.mixin.sound;

import de.tomalbrc.filament.sound.PolymerSoundBlock;
import de.tomalbrc.filament.sound.SoundFix;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnDestroyParticles", at = @At("TAIL"))
    private void filament$spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        SoundType soundType = null;
        if (blockState.getBlock() instanceof PolymerSoundBlock) {
            soundType = blockState.getSoundType();
        }
        else if (SoundFix.REMIXES.containsValue(blockState.getSoundType())) {
            soundType = blockState.getSoundType();
        }

        if (soundType != null)
            level.playSound(null, blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
    }
}
