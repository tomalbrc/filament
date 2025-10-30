package de.tomalbrc.filament.data.entity;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourList;
import de.tomalbrc.filament.entity.skill.Skill;
import de.tomalbrc.filament.entity.skill.ThreatTable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record EntityData(
        ResourceLocation id,
        Map<String, String> translations,
        EntityAnimationInfo animation,
        ResourceLocation entityType,
        EntityProperties properties,
        BehaviourList goals,
        BehaviourConfigMap behaviour,
        Set<ResourceLocation> entityTags,
        Map<ResourceLocation, Double> attributes,
        SpawnInfo spawn,
        ThreatTable threatTable,
        List<Skill> skills
) {

    public EntityData {
        // Ensure defaults similar to the original class
        if (entityType == null) {
            entityType = BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG);
        }

        if (properties == null) {
            properties = new EntityProperties();
        }

        if (behaviour == null) {
            behaviour = new BehaviourConfigMap();
        }

        if (goals == null) {
            goals = BehaviourList.EMPTY;
        }
    }
}