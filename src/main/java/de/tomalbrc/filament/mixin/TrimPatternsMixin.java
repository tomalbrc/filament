package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.FilamentTrimPatterns;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrimPatterns.class)
public class TrimPatternsMixin {
    @Inject(at= @At("TAIL"), method = "bootstrap")
    private static void filament$bootstrap(BootstrapContext<TrimPattern> bootstrapContext, CallbackInfo ci) {
        FilamentTrimPatterns.bootstrap(bootstrapContext);
    }
}
