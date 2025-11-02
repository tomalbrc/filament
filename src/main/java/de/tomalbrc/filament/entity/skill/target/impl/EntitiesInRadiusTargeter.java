package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;

import java.util.List;
import java.util.stream.Collectors;

public class EntitiesInRadiusTargeter implements Targeter {
    private final double radius;

    public EntitiesInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillTree context) {
        return context.getNearbyEntities(radius).stream()
                .map(Target::of)
                .collect(Collectors.toList());
    }
}