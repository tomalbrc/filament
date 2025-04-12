package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourList;
import de.tomalbrc.filament.data.properties.EntityProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class EntityData {
    private final @NotNull ResourceLocation id;
    private final @Nullable ResourceLocation entityType;
    private final @Nullable Map<String, String> translations;
    private final @Nullable AnimationInfo animation;
    private final @Nullable EntityProperties properties;
    private final @Nullable BehaviourConfigMap behaviour;
    private final @Nullable BehaviourList goals;
    private final @Nullable Set<ResourceLocation> entityTags;
    private final @Nullable Map<ResourceLocation, Double> attributes;

    protected EntityData(
            @NotNull ResourceLocation id,
            @Nullable Map<String, String> translations,
            @Nullable AnimationInfo animation,
            @Nullable ResourceLocation entityType,
            @Nullable EntityProperties properties,
            @Nullable BehaviourList goals,
            @Nullable BehaviourConfigMap behaviour,
            @Nullable Set<ResourceLocation> entityTags,
            @Nullable Map<ResourceLocation, Double> attributes
    ) {
        this.id = id;
        this.translations = translations;
        this.animation = animation;
        this.entityType = entityType;
        this.properties = properties;
        this.behaviour = behaviour;
        this.goals = goals;
        this.entityTags = entityTags;
        this.attributes = attributes;
    }

    public @NotNull ResourceLocation id() {
        return id;
    }

    public @NotNull ResourceLocation entityType() {
        return entityType == null ? BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG) : entityType;
    }

    public @Nullable Map<String, String> translations() {
        return translations;
    }

    public @Nullable AnimationInfo animation() {
        return animation;
    }

    public @NotNull BehaviourConfigMap behaviour() {
        return behaviour == null ? BehaviourConfigMap.EMPTY : behaviour;
    }

    public @Nullable Set<ResourceLocation> entityTags() {
        return entityTags;
    }

    public @Nullable Map<ResourceLocation, Double> attributes() {
        return attributes;
    }

    public @NotNull EntityProperties properties() {
        return properties == null ? EntityProperties.EMPTY : properties;
    }

    @NotNull
    public BehaviourList goals() {
        return goals == null ? BehaviourList.EMPTY : goals;
    }

    public record AnimationInfo(
            ResourceLocation model,
            String idleAnimation,
            String walkAnimation
    ) {

    }
}
