package de.tomalbrc.filament.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.sound.PolymerSoundBlock;
import de.tomalbrc.filament.sound.SoundFix;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// actually send step sounds, not just locally or for everyone but the player in case of the Player class
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract Level level();

    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Shadow public abstract SoundSource getSoundSource();

    @WrapOperation(method = "playCombinationStepSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void filament$combinationStepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if (FilamentConfig.getInstance().soundModule && (Object)this instanceof ServerPlayer && blockState.getBlock() instanceof PolymerSoundBlock || SoundFix.REMIXES.containsValue(blockState.getSoundType()))
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }

    @WrapOperation(method = "playMuffledStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void filament$muffledStepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if (FilamentConfig.getInstance().soundModule && (Object)this instanceof ServerPlayer && blockState.getBlock() instanceof PolymerSoundBlock || SoundFix.REMIXES.containsValue(blockState.getSoundType()))
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void filament$stepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if (FilamentConfig.getInstance().soundModule && (Object)this instanceof ServerPlayer && blockState.getBlock() instanceof PolymerSoundBlock || SoundFix.REMIXES.containsValue(blockState.getSoundType()))
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }
}
