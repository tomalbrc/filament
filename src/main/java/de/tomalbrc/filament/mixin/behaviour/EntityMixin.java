package de.tomalbrc.filament.mixin.behaviour;

import de.tomalbrc.filament.block.SimpleBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow private Level level;

    @Inject(method = "getBlockBounciness", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getBounceRestitution()F"))
    private void filament$blockBehaviourBounce(Block onBlock, CallbackInfoReturnable<Double> cir) {
        if (onBlock instanceof SimpleBlock simpleBlock) {
            simpleBlock.updateEntityMovementAfterFallOn(level, (Entity) (Object)this);
        }
    }
}
