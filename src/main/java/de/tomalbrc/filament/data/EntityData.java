package de.tomalbrc.filament.data;

import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourList;
import de.tomalbrc.filament.data.properties.EntityProperties;
import de.tomalbrc.filament.entity.BiomeHelper;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class EntityData {
    private final @NotNull Identifier id;
    private final @Nullable Identifier entityType;
    private final @Nullable Map<String, String> translations;
    private final @Nullable AnimationInfo animation;
    private @Nullable EntityProperties properties;
    @SerializedName(value = "behaviour", alternate = {"behaviours", "behaviors", "behavior"})
    private @Nullable BehaviourConfigMap behaviour;
    private final @Nullable BehaviourList goals;
    private final @Nullable Set<Identifier> entityTags;
    private final @Nullable Map<Identifier, Double> attributes;
    private final @Nullable SpawnInfo spawn;

    protected EntityData(
            @NotNull Identifier id,
            @Nullable Map<String, String> translations,
            @Nullable AnimationInfo animation,
            @Nullable Identifier entityType,
            @Nullable EntityProperties properties,
            @Nullable BehaviourList goals,
            @Nullable BehaviourConfigMap behaviour,
            @Nullable Set<Identifier> entityTags,
            @Nullable Map<Identifier, Double> attributes,
            @Nullable SpawnInfo spawn
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
        this.spawn = spawn;
    }

    public @NotNull Identifier id() {
        return id;
    }

    public @NotNull Identifier entityType() {
        return entityType == null ? BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG) : entityType;
    }

    public @Nullable Map<String, String> translations() {
        return translations;
    }

    public @Nullable AnimationInfo animation() {
        return animation;
    }

    public @NotNull BehaviourConfigMap behaviour() {
        if (behaviour == null)
            behaviour = new BehaviourConfigMap();
        return behaviour;
    }

    public @Nullable Set<Identifier> entityTags() {
        return entityTags;
    }

    public @Nullable Map<Identifier, Double> attributes() {
        return attributes;
    }

    public @NotNull EntityProperties properties() {
        if (properties == null) {
            properties = new EntityProperties();
        }

        return properties;
    }

    @NotNull
    public BehaviourList goals() {
        return goals == null ? BehaviourList.EMPTY : goals;
    }

    @Nullable
    public SpawnInfo spawn() {
        return spawn;
    }

    public record AnimationInfo(
            Identifier model,
            String idleAnimation,
            String walkAnimation
    ) {}

    public record SpawnInfo(
        int weight,
        int minGroupSize,
        int maxGroupSize,
        boolean foundInOverworld,
        boolean foundInNether,
        boolean foundInEnd,
        Set<Identifier> spawnsLike,
        Set<Identifier> biomes,
        Set<Identifier> biomeTags
    ) {
        public void add(EntityType<?> entityType) {
            Set<EntityType<?>> spawns = spawnsLike == null ? ImmutableSet.of() : null;
            if (spawns == null) {
                spawns = new ObjectArraySet<>();
                for (Identifier Identifier : spawnsLike) {
                    spawns.add(BuiltInRegistries.ENTITY_TYPE.getValue(Identifier));
                }
            }

            Predicate<BiomeSelectionContext> biomeSelectors = spawns.isEmpty() ? ((x) -> false) : BiomeSelectors.spawnsOneOf(spawns);
            if (biomes != null) biomeSelectors = biomeSelectors.or(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP));
            if (biomeTags != null) {
                for (Identifier tag : biomeTags) {
                    biomeSelectors = biomeSelectors.or(BiomeSelectors.tag(TagKey.create(Registries.BIOME, tag)));
                }
            }

            if (foundInOverworld) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInOverworld());
            if (foundInNether) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInTheNether());
            if (foundInEnd) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInTheEnd());

            BiomeHelper.addSpawn(entityType, weight, minGroupSize, maxGroupSize, biomeSelectors);
        }
    }
}
