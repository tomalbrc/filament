package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.util.FilamentConfig;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostConfig;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AutoHost.class)
public class AutoHostMixin {
    @Inject(remap = false, method = "init", at = @At(value = "FIELD", target = "Leu/pb4/polymer/autohost/impl/AutoHost;config:Leu/pb4/polymer/autohost/impl/AutoHostConfig;", opcode = Opcodes.PUTSTATIC))
    private static void filament$forceAutoHost(MinecraftServer server, CallbackInfo ci, @Local AutoHostConfig obj) {
        obj.enabled = FilamentConfig.getInstance().forceAutohost; // we need it enabled by default..
    }
}
