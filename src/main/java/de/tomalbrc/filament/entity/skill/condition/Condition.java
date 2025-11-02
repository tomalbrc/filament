package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;

public interface Condition {
    /** 
     * Evaluate a condition.
     * If target is null, the condition is considered a context/trigger condition.
     * If target is non-null, condition may check target-specific things (target conditions).
     */
    boolean test(SkillTree context, Target target);
}
