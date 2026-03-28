package de.tomalbrc.filament.mixin.datafix;

import com.mojang.serialization.Dynamic;
import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegionStorage.class)
public class ChunkStorageMixin {
    @Inject(method = "upgradeChunkTag(Lcom/mojang/serialization/Dynamic;I)Lcom/mojang/serialization/Dynamic;", at = @At(value = "RETURN"), cancellable = true)
    private void filament$addChunkFixer(Dynamic<Tag> chunkTag, int defaultVersion, CallbackInfoReturnable<Dynamic<Tag>> cir) {
        if (FilamentConfig.getInstance().version < DataFix.VERSION) {
            var res = DataFixTypes.CHUNK.update(DataFix.getDataFixer(), chunkTag, 1, DataFix.VERSION);
            cir.setReturnValue(res);
        }
    }
}
