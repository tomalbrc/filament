package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class IsFilamentMobCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        return target.getEntity() instanceof FilamentMob;
    }
}
