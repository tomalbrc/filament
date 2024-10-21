package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.util.Util;
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
}