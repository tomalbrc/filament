package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.Leashable;

class IsLeashedCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return target instanceof Leashable leashable && leashable.isLeashed();
    }
}
