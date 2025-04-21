package de.tomalbrc.filament.mixin.sound;

import de.tomalbrc.filament.sound.PolymerSoundBlock;
import de.tomalbrc.filament.sound.SoundFix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// handle mining sounds
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow protected ServerLevel level;

    @Shadow private int gameTicks;

    @Inject(method = "incrementDestroyProgress", at = @At("HEAD"))
    private void filament$soundMine(BlockState blockState, BlockPos blockPos, int startTime, CallbackInfoReturnable<Float> cir) {
        var destroyTicks = (gameTicks - startTime) - 1;
        if ((blockState.getBlock() instanceof PolymerSoundBlock || SoundFix.REMIXES.containsValue(blockState.getSoundType())) && destroyTicks % 4 == 0) {
            SoundType soundType = blockState.getSoundType();
            level.playSound(null, blockPos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 8.0f, soundType.getPitch() * 0.5f);
        }
    }
}
