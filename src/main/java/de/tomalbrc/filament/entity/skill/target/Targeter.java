package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillTree;

import java.util.List;

public interface Targeter {
    List<Target> find(SkillTree context);
}