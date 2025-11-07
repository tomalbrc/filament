package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import org.jetbrains.annotations.Nullable;

public record Skill(
        Mechanic mechanic,
        SkillTrigger trigger,
        int time,
        Targeter targeter,
        @Nullable SkillHealthCondition healthCondition,
        @Nullable Double chance,
        @Nullable Condition condition
) {
    public boolean canRun(SkilledEntity<?> parent) {
        if (healthCondition() != null && !healthCondition().isMet(parent)) return false;
        return chance() == null || !(chance() < 1.0) || !(parent.getRandom().nextDouble() > chance());
    }
}