package de.tomalbrc.filament.mixin.component;

import de.tomalbrc.filament.injection.DataComponentCopying;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin implements DataComponentCopying {
    @Shadow @Final private List<DataComponentInitializers.InitializerEntry<?>> initializers;
    @Unique List<CustomInitializerEntry> filament$copyEntries = new ObjectArrayList<>();

    @Inject(method = "runInitializers", at = @At("RETURN"))
    private void filament$copyComponents(HolderLookup.Provider context, CallbackInfoReturnable<Map<ResourceKey<?>, DataComponentMap.Builder>> cir) {
        var map = cir.getReturnValue();

        for (CustomInitializerEntry entry : filament$copyEntries) {
            boolean foundEntry = false;
            for (DataComponentInitializers.InitializerEntry<?> initializer : initializers) {
                if (initializer.key().equals(entry.source())) {
                    entry.customPatcher().apply(initializer, map.get(entry.target()), context);
                    foundEntry = true;
                    break;
                }
            }

            if (!foundEntry) {
                entry.customPatcher().apply(null, map.get(entry.target()), context);
            }
        }
    }

    @Override
    public void filament$registerToCopy(CustomInitializerEntry entry) {
        filament$copyEntries.add(entry);
    }
}
