package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.registry.filament.StrippableRegistry;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @Inject(method = "getStripped", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At("RETURN"), cancellable = true)
    private void filament$onGetStripped(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (StrippableRegistry.has(blockState)) {
            var ns = StrippableRegistry.getStrippable(blockState);
            for (Property property : blockState.getProperties()) {
                if (ns.hasProperty(property)) {
                    ns.setValue(property, blockState.getValue(property));
                }
            }
            cir.setReturnValue(Optional.of(ns));
        }
    }
}
