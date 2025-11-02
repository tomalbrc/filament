package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.Leashable;

public class IsLeashedCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        return target instanceof Leashable leashable && leashable.isLeashed();
    }
}
