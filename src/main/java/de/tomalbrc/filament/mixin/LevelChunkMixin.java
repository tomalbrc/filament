package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(
            method = "setBlockEntity",
            at = @At("TAIL")
    )
    private void filament$filamentDecorationInit(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof BlockEntityWithElementHolder decorationBlockEntity)
            decorationBlockEntity.attach((LevelChunk)(Object) this);
    }
}
