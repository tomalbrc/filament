package de.tomalbrc.filament.mixin.sound;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.tomalbrc.filament.sound.SoundFix;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @ModifyReturnValue(method = "getSoundType", at = @At("RETURN"))
    private SoundType filament$modifySoundType(SoundType original) {
        if (SoundFix.REMIXES.containsKey(original))
            return SoundFix.REMIXES.get(original);

        return original;
    }
}
