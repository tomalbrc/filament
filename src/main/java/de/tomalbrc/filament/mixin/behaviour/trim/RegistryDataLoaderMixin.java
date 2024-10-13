package de.tomalbrc.filament.mixin.behaviour.trim;

import de.tomalbrc.filament.trim.FilamentTrimPatterns;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Inject(at = @At(value = "TAIL"), method = "method_56514")
    private static void filament$bootstrap(ResourceManager resourceManager, RegistryDataLoader.Loader loader, RegistryOps.RegistryInfoLookup registryInfoLookup, CallbackInfo ci) {
        if (loader.registry() instanceof MappedRegistry mappedRegistry) {
            if (mappedRegistry.key() == Registries.TRIM_PATTERN) {
                FilamentTrimPatterns.bootstrap((WritableRegistry<TrimPattern>) mappedRegistry);
            }
        }
    }
}
