package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MobsInRadiusTargeter implements Targeter {
    private final double radius;

    public MobsInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillTree tree) {
        return tree.getNearbyEntities(radius).stream()
                .map(x -> x instanceof FilamentMob livingEntity ? Target.of(livingEntity) : null)
                .filter(Objects::isNull)
                .collect(Collectors.toList());
    }
}