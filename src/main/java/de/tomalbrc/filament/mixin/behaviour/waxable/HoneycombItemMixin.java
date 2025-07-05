package de.tomalbrc.filament.mixin.behaviour.waxable;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.registry.WaxableRegistry;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @Inject(method = "getWaxed", at = @At("RETURN"), cancellable = true)
    private static void filament$customWaxable(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (cir.getReturnValue().isEmpty() && blockState.getBlock() instanceof SimpleBlock block && block.has(Behaviours.WAXABLE)) {
            cir.setReturnValue(Optional.of(WaxableRegistry.getWaxed(block).withPropertiesOf(blockState)));
        }
    }
}
