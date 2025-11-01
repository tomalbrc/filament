package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

public interface Condition {
    /** 
     * Evaluate a condition.
     * If target is null, the condition is considered a context/trigger condition.
     * If target is non-null, condition may check target-specific things (target conditions).
     */
    boolean test(SkillContext context, Target target);
}
