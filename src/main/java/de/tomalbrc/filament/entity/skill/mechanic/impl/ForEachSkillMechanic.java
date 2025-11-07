package de.tomalbrc.filament.entity.skill.mechanic.impl;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.Skill;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.meta.MetaSkill;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.registry.MetaSkillRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ForEachSkillMechanic implements Mechanic {
    private final ResourceLocation skill;

    transient private MetaSkill metaSkill;

    public ForEachSkillMechanic(ResourceLocation skill) {
        this.skill = skill;
    }

    @Override
    public ExecutionResult execute(SkillTree context) {
        if (metaSkill == null)
            metaSkill = MetaSkillRegistry.get(skill);

        List<Skill> skillList = null;

        if (context.mobSkills().isOnCooldown(skill, context.caster().level())) {
            Skill cds = metaSkill.onCooldownSkill();
            if (cds != null) {
                skillList = ImmutableList.of(cds);
            } else return null;
        } else {
            // TODO: condition actions to fire even more skills
            final var ct = Target.of(context.caster());
            boolean success = metaSkill.conditions() == null || metaSkill.conditions().stream().allMatch(x -> x.test(context, ct));

            // TODO: filter instead if testing
            boolean success2 = metaSkill.targetConditions() == null || metaSkill.targetConditions().stream().allMatch(x -> x.test(context, context.trigger()));

            if (success && success2) {
                skillList = metaSkill.skills();
                if (metaSkill.skill() != null) {
                    // TODO: pass target conditions

                    context.mobSkills().submitTree(context.copyWith(skillList));
                }
            } else if (metaSkill.failedConditionsSkill() != null) {
                skillList = ImmutableList.of(metaSkill.failedConditionsSkill());
            }
        }

        if (skillList != null) {
            for (Target target : context.getCurrentTargets()) {
                context.mobSkills().submitTree(context.copyWith(skillList, List.of(target)));
            }
        }

        return null;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.SKILL;
    }
}
