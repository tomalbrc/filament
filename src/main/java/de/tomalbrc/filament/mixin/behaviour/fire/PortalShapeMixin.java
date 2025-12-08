package de.tomalbrc.filament.mixin.behaviour.fire;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Fire;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {
    @Inject(method = "isEmpty", at = @At(value = "RETURN"), cancellable = true)
    private static void filament$isEmpty(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        Fire f;
        if (!cir.getReturnValue() && blockState.getBlock().isFilamentBlock() && (f = blockState.getBlock().get(Behaviours.FIRE)) != null && f.getConfig().lightPortal) {
            cir.setReturnValue(true);
        }
    }
}
