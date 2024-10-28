package de.tomalbrc.filament.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
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

@SuppressWarnings("unused")
public class ItemData {
    protected ResourceLocation id;

    @Nullable
    protected Item vanillaItem;

    @Nullable
    protected ItemResource itemResource;

    @SerializedName("behaviour")
    @Nullable
    protected BehaviourConfigMap behaviourConfig;

    @Nullable
    protected ItemProperties properties;

    @Nullable
    protected DataComponentMap components;

    @SerializedName("group")
    @Nullable
    protected ResourceLocation itemGroup;

    protected Map<DataComponentType<?>, JsonObject> additionalComponents;

    public void set(DataComponentType<?> s, JsonObject jsonObject) {
        if (this.additionalComponents == null) this.additionalComponents = new Object2ObjectOpenHashMap<>();
        this.additionalComponents.put(s, jsonObject);
    }

    public Map<DataComponentType<?>, JsonObject> getAdditionalComponents() {
        return additionalComponents;
    }

    @NotNull
    public ItemProperties properties() {
        if (properties == null) {
            return ItemProperties.EMPTY;
        }
        return properties;
    }

    @NotNull
    public DataComponentMap components() {
        if (components == null) {
            return DataComponentMap.EMPTY;
        }
        return components;
    }

    @NotNull
    public Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    @NotNull
    public ResourceLocation id() {
        return id;
    }

    @Nullable
    public ResourceLocation itemGroup() {
        return itemGroup;
    }

    @Nullable
    public BehaviourConfigMap behaviourConfig() {
        return behaviourConfig;
    }

    @Nullable
    public ItemResource itemResource() {
        return itemResource;
    }
}
