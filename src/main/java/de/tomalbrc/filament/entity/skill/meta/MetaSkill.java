package de.tomalbrc.filament.entity.skill.meta;

import de.tomalbrc.filament.entity.skill.Skill;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;

public record MetaSkill(
        ResourceLocation id,
        boolean cancelIfNoTargets,
        List<Condition> conditions,
        List<Condition> targetConditions,
        List<Condition> triggerConditions,
        Skill failedConditionsSkill,
        int cooldown,
        Skill onCooldownSkill,
        Skill skill,
        List<Skill> skills,
        Map<String, Object> skillVariables
) {

}
