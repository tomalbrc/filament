package de.tomalbrc.filament.data;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Data<PropertyType extends ItemProperties> {
    protected final @NotNull ResourceLocation id;
    protected final @Nullable Item vanillaItem;
    protected final @Nullable Map<String, String> translations;
    protected final @Nullable Component displayName;
    protected final @Nullable ItemResource itemResource;
    protected final @Nullable ResourceLocation itemModel;
    protected final @Nullable BehaviourConfigMap behaviour;
    protected final @Nullable DataComponentMap components;
    protected final @Nullable ResourceLocation group;
    protected final @Nullable Set<ResourceLocation> itemTags;
    protected @Nullable PropertyType properties;

    transient protected Map<DataComponentType<?>, JsonElement> additionalComponents;

    protected Data(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
            @Nullable ResourceLocation itemModel,
            @Nullable PropertyType properties,
            @Nullable BehaviourConfigMap behaviour,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation group,
            @Nullable Set<ResourceLocation> itemTags
            ) {
        this.id = id;
        this.vanillaItem = vanillaItem;
        this.translations = translations;
        this.displayName = displayName;
        this.itemResource = itemResource;
        this.itemModel = itemModel;
        this.properties = properties;
        this.behaviour = behaviour;
        this.components = components;
        this.group = group;
        this.itemTags = itemTags;
    }

    public void putAdditional(DataComponentType<?> s, JsonElement jsonObject) {
        if (this.additionalComponents == null) this.additionalComponents = new Object2ObjectOpenHashMap<>();
        this.additionalComponents.put(s, jsonObject);
    }

    @NotNull
    public Map<DataComponentType<?>, JsonElement> getAdditionalComponents() {
        if (this.additionalComponents == null) this.additionalComponents = new Object2ObjectOpenHashMap<>();
        return additionalComponents;
    }

    public abstract @NotNull ItemProperties properties();

    public @NotNull ResourceLocation id() {
        return id;
    }

    public @NotNull Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    public @Nullable Map<String, String> translations() {
        return translations;
    }

    public @Nullable ItemResource itemResource() {
        return itemResource;
    }

    public @Nullable ResourceProvider preferredResource() {
        return itemResource();
    }

    public @Nullable ResourceLocation itemModel() {
        return itemModel;
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

    public @Nullable Set<ResourceLocation> itemTags() { return this.itemTags; }

    public @Nullable Component displayName() {
        return this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Data<?>) obj;
        return Objects.equals(this.id, that.id);
    }
}
