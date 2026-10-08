package de.tomalbrc.filament.mixin.decoration;

import de.tomalbrc.filament.decoration.DecorationPreviewManager;
import de.tomalbrc.filament.injection.DecorationPreviewHolder;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements DecorationPreviewHolder {
    @Unique
    DecorationPreviewManager.PreviewState filament$previewState;

    @Inject(method = "tick", at = @At("TAIL"))
    private void filament$tickPreview(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isDeadOrDying() || self.isRemoved()) {
            DecorationPreviewManager.clear(self);
            return;
        }

        DecorationPreviewManager.tick(self);
    }

    @Override
    public @Nullable DecorationPreviewManager.PreviewState filament$getPreviewState() {
        return filament$previewState;
    }

    @Override
    public void filament$setPreviewState(@Nullable DecorationPreviewManager.PreviewState state) {
        filament$previewState = state;
    }
}