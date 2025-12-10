package de.tomalbrc.filament.data;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Data<PropertyType extends ItemProperties> {
    protected final @NotNull Identifier id;
    protected final @Nullable Item vanillaItem;
    protected final @Nullable Map<String, String> translations;
    protected final @Nullable Component displayName;
    protected @Nullable ItemResource itemResource;
    protected final @Nullable Identifier itemModel;
    @SerializedName(value = "behaviour", alternate = {"behaviours", "behaviors", "behavior"})
    protected @Nullable BehaviourConfigMap behaviour;
    protected final @Nullable DataComponentMap components;
    protected final @Nullable Identifier group;
    protected final @Nullable Set<Identifier> itemTags;
    protected @Nullable PropertyType properties;

    transient protected Map<DataComponentType<?>, JsonElement> additionalComponents;

    protected Data(
            @NotNull Identifier id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
            @Nullable Identifier itemModel,
            @Nullable PropertyType properties,
            @Nullable BehaviourConfigMap behaviour,
            @Nullable DataComponentMap components,
            @Nullable Identifier group,
            @Nullable Set<Identifier> itemTags
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

    public @NotNull Identifier id() {
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

    public void setItemResource(ItemResource itemResource) {
        this.itemResource = itemResource;
    }

    public @Nullable ResourceProvider preferredResource() {
        return itemResource();
    }

    public @Nullable Identifier itemModel() {
        return itemModel;
    }

    public @NotNull BehaviourConfigMap behaviour() {
        if (behaviour == null) {
            behaviour = new BehaviourConfigMap();
        }
        return behaviour;
    }

    public @NotNull DataComponentMap components() {
        return components == null ? DataComponentMap.EMPTY : components;
    }

    public @Nullable Identifier group() {
        return group;
    }

    public @Nullable Set<Identifier> itemTags() { return this.itemTags; }

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
