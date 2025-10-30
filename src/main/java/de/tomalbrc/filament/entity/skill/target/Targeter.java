package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillContext;

import java.util.List;

public interface Targeter {
    List<Target> find(SkillContext context);
}