package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillContext;

import java.util.List;

public class TargetTargeter implements Targeter {
    @Override
    public List<Target> find(SkillContext context) {
        return List.of(Target.of(context.caster().getTarget()));
    }
}