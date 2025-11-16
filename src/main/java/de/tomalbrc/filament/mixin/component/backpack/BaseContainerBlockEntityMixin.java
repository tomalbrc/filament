package de.tomalbrc.filament.mixin.component.backpack;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin {
    @Unique
    FilamentComponents.BackpackOptions filament$backpackOptions;

    @Inject(method = "applyImplicitComponents", at = @At(value = "TAIL"))
    private void filament$backpackApplyImplicitComponents(BlockEntity.DataComponentInput dataComponentGetter, CallbackInfo ci) {
        var bp = dataComponentGetter.get(FilamentComponents.BACKPACK);
        if (bp != null)
            filament$backpackOptions = bp;
    }

    @Inject(method = "collectImplicitComponents", at = @At(value = "TAIL"))
    private void filament$backpackCollectImplicitComponents(DataComponentMap.Builder builder, CallbackInfo ci) {
        if (filament$backpackOptions != null)
            builder.set(FilamentComponents.BACKPACK, filament$backpackOptions);
    }

    @Inject(method = "removeComponentsFromTag", at = @At(value = "TAIL"))
    private void filament$backpackRemoveComponentsFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.remove("Backpack");
    }

    @Inject(method = "loadAdditional", at = @At(value = "TAIL"))
    private void filament$backpackLoadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (compoundTag.contains("Backpack")) {
            this.filament$backpackOptions = FilamentComponents.BackpackOptions.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), compoundTag.get("Backpack")).getOrThrow().getFirst();
        }
    }

    @Inject(method = "saveAdditional", at = @At(value = "TAIL"))
    private void filament$backpackSaveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (this.filament$backpackOptions != null) {
            compoundTag.put("Backpack", FilamentComponents.BackpackOptions.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), filament$backpackOptions).getOrThrow());
        }
    }
}
