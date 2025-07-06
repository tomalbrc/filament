package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.registry.BiomeModifications;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/RegistryLayer;createRegistryAccess()Lnet/minecraft/core/LayeredRegistryAccess;", shift = At.Shift.BEFORE))
    private static void filament$loadEarly(WorldLoader.InitConfig initConfig, WorldLoader.WorldDataSupplier worldDataSupplier, WorldLoader.ResultFactory resultFactory, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture> cir, @Local CloseableResourceManager closeableResourceManager) {
        Util.loadDatapackContents(closeableResourceManager);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Ljava/util/List;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <D, R> void filament$almostDone(WorldLoader.InitConfig initConfig, WorldLoader.WorldDataSupplier<D> worldDataSupplier, WorldLoader.ResultFactory<D, R> resultFactory, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture<R>> cir, @Local(ordinal = 1) LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        BiomeModifications.addAll(layeredRegistryAccess);
    }
}