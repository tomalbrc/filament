package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FilePackResources.class)
public class FilePackResourcesMixin {
    @Inject(method = "listResources", at = @At(value = "INVOKE", target = "Ljava/util/Enumeration;hasMoreElements()Z"))
    private void filament$removeExcessSlashes(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput, CallbackInfo ci, @Local(ordinal = 3) LocalRef<String> string4) {
        if (string4.get().endsWith("//")) {
            string4.set(string4.get().substring(0, string4.get().length()-2));
        }
    }
}