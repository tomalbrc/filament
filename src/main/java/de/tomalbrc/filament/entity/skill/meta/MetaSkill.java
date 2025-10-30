package de.tomalbrc.filament.entity.skill.meta;

import de.tomalbrc.filament.entity.skill.Skill;
import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
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
        SkillSequence skills,
        List<Entity> targets,
        Map<String, Object> skillVariables
) implements Mechanic {

    @Override
    public int execute(SkillContext context) {
        this.skills.tick(context);
        return 0;
    }
}
