package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;

import java.util.List;
import java.util.stream.Collectors;

public class TrackedTargeter implements Targeter {
    @Override
    public List<Target> find(SkillTree context) {
        return context.caster.getTracking().stream()
                .map(Target::of)
                .collect(Collectors.toList());
    }
}