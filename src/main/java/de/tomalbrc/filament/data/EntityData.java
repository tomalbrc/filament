package de.tomalbrc.filament.data;

import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourList;
import de.tomalbrc.filament.data.properties.EntityProperties;
import de.tomalbrc.filament.entity.BiomeHelper;
import de.tomalbrc.filament.util.RuntimeTypeAdapterFactory;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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

    private Movement movement = new Movement();

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
            @Nullable SpawnInfo spawn,
            @NonNull Movement movement
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
        this.movement = movement;
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

    public @NonNull Movement movement() {
        return movement;
    }

    public static class Movement {
        public MovementType movementType = new DefaultMovementType();
        public JumpType jumpType = new DefaultJumpType();
        public NavigationType navigationType = new GroundNavigationType();
        public Map<PathType, Float> pathfindingMalus = Map.of();
    }

    public interface JumpType {
        RuntimeTypeAdapterFactory<JumpType> ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(EntityData.JumpType.class, "type")
                .registerSubtype(EntityData.DefaultJumpType.class, "default");

                @NonNull
        JumpControl getControl(PathfinderMob mob);
    }

    public interface NavigationType {
        RuntimeTypeAdapterFactory<NavigationType> ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(EntityData.NavigationType.class, "type")
                .registerSubtype(EntityData.GroundNavigationType.class, "ground")
                .registerSubtype(EntityData.WaterBoundNavigationType.class, "water_bound")
                .registerSubtype(EntityData.WallClimberNavigationType.class, "wall_climber")
                .registerSubtype(EntityData.FlyingNavigationType.class, "flying")
                .registerSubtype(EntityData.AmphibiousNavigationType.class, "amphibious");

                @NonNull PathNavigation get(PathfinderMob mob, Level level);
    }

    public record GroundNavigationType() implements NavigationType {
        @Override
        public @NonNull PathNavigation get(PathfinderMob mob, Level level) {
            return new GroundPathNavigation(mob, level);
        }
    }
    public record FlyingNavigationType() implements NavigationType {
        @Override
        public @NonNull PathNavigation get(PathfinderMob mob, Level level) {
            return new FlyingPathNavigation(mob, level);
        }
    }
    public record AmphibiousNavigationType() implements NavigationType {
        @Override
        public @NonNull PathNavigation get(PathfinderMob mob, Level level) {
            return new AmphibiousPathNavigation(mob, level);
        }
    }
    public record WaterBoundNavigationType() implements NavigationType {
        @Override
        public @NonNull PathNavigation get(PathfinderMob mob, Level level) {
            return new WaterBoundPathNavigation(mob, level);
        }
    }
    public record WallClimberNavigationType() implements NavigationType {
        @Override
        public @NonNull PathNavigation get(PathfinderMob mob, Level level) {
            return new WallClimberNavigation(mob, level);
        }
    }

    public record DefaultJumpType() implements JumpType {
        @Override
        public @NonNull JumpControl getControl(PathfinderMob mob) {
            return new JumpControl(mob);
        }
    }

    public interface MovementType {
        RuntimeTypeAdapterFactory<MovementType> ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(EntityData.MovementType.class, "type")
                .registerSubtype(EntityData.DefaultMovementType.class, "default")
                    .registerSubtype(EntityData.SmoothSwimmingMovementType.class, "smooth_swimming")
                    .registerSubtype(EntityData.FlyingMovementType.class, "flying");

        @NonNull MoveControl getControl(PathfinderMob mob);
    }

    public record DefaultMovementType() implements MovementType {
        @Override
        public @NonNull MoveControl getControl(PathfinderMob mob) {
            return new MoveControl(mob);
        }
    }
    public record SmoothSwimmingMovementType(int maxTurnX, int maxTurnY, float waterSpeedMod, float outsideWaterSpeedMod, boolean applyGravity) implements MovementType {
        @Override
        public @NonNull MoveControl getControl(PathfinderMob mob) {
            return new SmoothSwimmingMoveControl(mob, maxTurnX, maxTurnY, waterSpeedMod, outsideWaterSpeedMod, applyGravity);
        }
    }
    public record FlyingMovementType(int maxTurn, boolean hoversInPlace) implements MovementType {
        @Override
        public @NonNull MoveControl getControl(PathfinderMob mob) {
            return new FlyingMoveControl(mob, maxTurn, hoversInPlace);
        }
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
