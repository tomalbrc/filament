package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.util.mixin.RegistryUnfreezer;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RegistryUnfreezer {
    @Shadow
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow
    private boolean frozen;

    @Shadow @Final private Map<Identifier, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;

    @Shadow private MappedRegistry.TagSet<T> allTags;

    @Shadow public abstract Registry<T> freeze();

    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow public abstract Optional<Holder.Reference<T>> get(int id);
    @Shadow public abstract Optional<Holder.Reference<T>> get(Identifier id);
    @Shadow @Final private Reference2IntMap<T> toId;

    @Unique boolean filament$wasFrozen;

    @Override
    @Unique
    public void filament$unfreeze() {
        if (this.frozen) {
            this.unregisteredIntrusiveHolders = new Reference2ObjectOpenHashMap<>();
            this.filament$wasFrozen = this.frozen;
            this.frozen = false;
        }
    }

    @Override
    @Unique
    public void filament$freeze() {
        this.frozen = true;
    }

    @Override
    @Unique
    public void filament$hackyRemove(Object t, ResourceKey key) {
        var i = get(key.identifier());
        if (i.isPresent()) {
            this.byId.remove(i.get());
            this.toId.removeInt(t);
            this.byLocation.remove(key.identifier());
            this.byKey.remove(key);
            this.byValue.remove((T)t);
            this.registrationInfos.remove(key);
        }
    }

    @WrapOperation(method = "lambda$refreshTagsInHolders$1", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean filament$hackyhack(List instance, T e, Operation<Boolean> original, @Local(argsOnly = true, name = "tagsForElement") Map<Holder.Reference, List> tagsForElement, @Local(name = "reference") Holder.Reference reference) {
        // modifying that TagSet sounds like hell, maybe this works better
        if (instance == null) {
            instance = new ArrayList<>();
            tagsForElement.put(reference, instance);
        }

        return original.call(instance, e);
    }
}
