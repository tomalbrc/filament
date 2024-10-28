package de.tomalbrc.filament.data;

import com.google.gson.JsonObject;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.resource.ItemResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class Data {
    protected final @NotNull ResourceLocation id;
    protected final @Nullable Item vanillaItem;
    protected final @Nullable ItemResource itemResource;
    protected final @Nullable BehaviourConfigMap behaviour;
    protected final @Nullable DataComponentMap components;
    protected final @Nullable ResourceLocation group;
    transient protected Map<DataComponentType<?>, JsonObject> additionalComponents;

    protected Data(@NotNull ResourceLocation id, @Nullable Item vanillaItem, @Nullable ItemResource itemResource, @Nullable BehaviourConfigMap behaviour, @Nullable DataComponentMap components, @Nullable ResourceLocation group) {
        this.id = id;
        this.vanillaItem = vanillaItem;
        this.itemResource = itemResource;
        this.behaviour = behaviour;
        this.components = components;
        this.group = group;
    }

    public void putAdditional(DataComponentType<?> s, JsonObject jsonObject) {
        if (this.additionalComponents == null) this.additionalComponents = new Object2ObjectOpenHashMap<>();
        this.additionalComponents.put(s, jsonObject);
    }

    @NotNull
    public Map<DataComponentType<?>, JsonObject> getAdditionalComponents() {
        if (this.additionalComponents == null) this.additionalComponents = new Object2ObjectOpenHashMap<>();
        return additionalComponents;
    }

    public @NotNull ResourceLocation id() {
        return id;
    }

    public @NotNull Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    public @Nullable ItemResource itemResource() {
        return itemResource;
    }

    public @NotNull BehaviourConfigMap behaviour() {
        return behaviour == null ? BehaviourConfigMap.EMPTY : behaviour;
    }

    public @NotNull DataComponentMap components() {
        return components == null ? DataComponentMap.EMPTY : components;
    }

    public @Nullable ResourceLocation group() {
        return group;
    }
}
