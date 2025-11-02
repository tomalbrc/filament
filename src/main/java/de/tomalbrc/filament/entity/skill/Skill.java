package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import org.jetbrains.annotations.Nullable;

public record Skill(
        Mechanic mechanic,
        Targeter targeter,
        SkillTrigger trigger,
        int time,
        @Nullable SkillHealthCondition healthCondition,
        @Nullable Double chance
) {

    public boolean canRun(FilamentMob parent) {
        if (healthCondition() != null && !healthCondition().isMet(parent)) return false;
        return chance() == null || !(chance() < 1.0) || !(parent.getRandom().nextDouble() > chance());
    }
}