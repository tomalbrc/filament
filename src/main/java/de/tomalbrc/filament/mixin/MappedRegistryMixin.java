package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.util.mixin.RegistryUnfreezer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> implements RegistryUnfreezer {
    @Shadow
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow
    private boolean frozen;

    @Unique boolean filament$wasFrozen;

    public void filament$unfreeze() {
        this.unregisteredIntrusiveHolders = new Reference2ObjectOpenHashMap<>();
        this.filament$wasFrozen = this.frozen;
        this.frozen = false;
    }

    public void filament$freeze() {
        this.frozen = true;
    }
}
