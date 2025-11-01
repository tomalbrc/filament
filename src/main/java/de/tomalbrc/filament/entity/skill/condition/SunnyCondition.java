package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class SunnyCondition implements Condition { public boolean test(SkillContext ctx, Target target){ return !ctx.level().isRaining(); } }
