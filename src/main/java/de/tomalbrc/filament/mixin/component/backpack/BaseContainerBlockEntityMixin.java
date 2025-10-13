package de.tomalbrc.filament.mixin.component.backpack;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private void filament$backpackApplyImplicitComponents(DataComponentGetter dataComponentGetter, CallbackInfo ci) {
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
    private void filament$backpackRemoveComponentsFromTag(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.discard("Backpack");
    }

    @Inject(method = "loadAdditional", at = @At(value = "TAIL"))
    private void filament$backpackLoadAdditional(ValueInput valueInput, CallbackInfo ci) {
        valueInput.read("Backpack", FilamentComponents.BackpackOptions.CODEC).ifPresent(x -> {
            this.filament$backpackOptions = x;
        });
    }

    @Inject(method = "saveAdditional", at = @At(value = "TAIL"))
    private void filament$backpackSaveAdditional(ValueOutput valueOutput, CallbackInfo ci) {
        if (this.filament$backpackOptions != null) {
            valueOutput.store("Backpack", FilamentComponents.BackpackOptions.CODEC, filament$backpackOptions);
        }
    }
}
