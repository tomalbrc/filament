package de.tomalbrc.filament.mixin;

import com.mojang.serialization.Lifecycle;
import de.tomalbrc.filament.registry.RegistryUnfreezer;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> implements RegistryUnfreezer {
    @Shadow
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow
    @Nullable
    private List<Holder.Reference<T>> holdersInOrder;

    @Shadow
    private boolean frozen;

    boolean isIntrusive = false;

    public void filament$unfreeze() {
        if (this.isIntrusive) this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
        this.frozen = false;
        this.holdersInOrder = null;
    }

    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
    private void filament$isIntr(ResourceKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
        this.isIntrusive = intrusive;
    }
}
