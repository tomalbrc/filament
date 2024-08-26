package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.registry.RegistryUnfreezer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> implements RegistryUnfreezer {
    @Shadow
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow
    private boolean frozen;

    public void filament$unfreeze() {
        this.unregisteredIntrusiveHolders = new Reference2ObjectOpenHashMap<>();
        this.frozen = false;
    }
}
