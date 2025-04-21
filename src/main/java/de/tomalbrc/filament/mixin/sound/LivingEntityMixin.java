package de.tomalbrc.filament.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.sound.PolymerSoundBlock;
import de.tomalbrc.filament.sound.SoundFix;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// play fall sounds serverside
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "playBlockFallSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void filament$stepSounds(LivingEntity instance, SoundEvent soundEvent, float vol, float pitch, Operation<Void> original, @Local BlockState blockState) {
        if (FilamentConfig.getInstance().soundModule && (Object)this instanceof ServerPlayer && blockState.getBlock() instanceof PolymerSoundBlock || SoundFix.REMIXES.containsValue(blockState.getSoundType()))
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), vol, pitch);
        else
            original.call(instance, soundEvent, vol, pitch);
    }
}
