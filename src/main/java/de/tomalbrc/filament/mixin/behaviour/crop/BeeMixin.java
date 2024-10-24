package de.tomalbrc.filament.mixin.behaviour.crop;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.block.SimpleBlock;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// For crops
@Mixin(Bee.BeeGrowCropGoal.class)
public abstract class BeeMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    void filament$beeAddCustom(CallbackInfo ci, @Local(ordinal = 0) BlockState blockState, @Local(ordinal=1) LocalRef<BlockState> blockState2) {
        if (blockState.getBlock() instanceof SimpleBlock simpleBlock && simpleBlock.has(Behaviours.CROP) && simpleBlock.get(Behaviours.CROP).getConfig().beeInteraction) {
            blockState2.set(simpleBlock.get(Behaviours.CROP).incrementedAge(blockState));
        }
    }
}
