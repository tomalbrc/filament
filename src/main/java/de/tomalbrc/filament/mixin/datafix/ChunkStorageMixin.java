package de.tomalbrc.filament.mixin.datafix;

import com.mojang.serialization.MapCodec;
import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
public class ChunkStorageMixin {
    @Inject(method = "upgradeChunkTag", at = @At(value = "RETURN"), cancellable = true)
    private void filament$addChunkFixer(ResourceKey<Level> levelKey, Supplier<DimensionDataStorage> storage, CompoundTag chunkData, Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> chunkGeneratorKey, CallbackInfoReturnable<CompoundTag> cir) {
        if (FilamentConfig.getInstance().version < DataFix.VERSION) {
            var res = DataFixTypes.CHUNK.update(DataFix.getDataFixer(), chunkData, 1, DataFix.VERSION);
            cir.setReturnValue(res);
        }
    }
}
