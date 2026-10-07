package de.tomalbrc.filament.mixin.decoration;

import de.tomalbrc.filament.decoration.DecorationPreviewManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void filament$tickPreview(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isDeadOrDying() || self.isRemoved()) {
            DecorationPreviewManager.clear(self);
            return;
        }

        DecorationPreviewManager.tick(self);
    }
}