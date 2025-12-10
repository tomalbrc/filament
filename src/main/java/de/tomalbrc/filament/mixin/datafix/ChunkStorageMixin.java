package de.tomalbrc.filament.mixin.datafix;

import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegionStorage.class)
public class ChunkStorageMixin {
    @Inject(method = "upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "RETURN"), cancellable = true)
    private void filament$addChunkFixer(CompoundTag compoundTag, int i, CompoundTag compoundTag2, CallbackInfoReturnable<CompoundTag> cir) {
        if (FilamentConfig.getInstance().version < DataFix.VERSION) {
            var res = DataFixTypes.CHUNK.update(DataFix.getDataFixer(), compoundTag, 1, DataFix.VERSION);
            cir.setReturnValue(res);
        }
    }
}
