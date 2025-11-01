package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import org.jetbrains.annotations.Nullable;

public record Skill(
        Mechanic mechanic,
        Targeter targeter,
        Trigger trigger,
        int time,
        @Nullable SkillHealthCondition healthCondition,
        @Nullable Double chance
) {

}