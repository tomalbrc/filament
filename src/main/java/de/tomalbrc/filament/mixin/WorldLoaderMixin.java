package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.registry.RegistryUnfreezer;
import de.tomalbrc.filament.util.FilamentReloadUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader;loadAndReplaceLayer(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;", shift = At.Shift.BEFORE), method = "load")
    private static void filament$loadEarly(WorldLoader.InitConfig initConfig, WorldLoader.WorldDataSupplier worldDataSupplier, WorldLoader.ResultFactory resultFactory, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture> cir, @Local CloseableResourceManager closeableResourceManager) {
        Util.loadDatapackContents(closeableResourceManager);
    }
}