package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;

class OutsideCondition implements Condition { public boolean test(SkillContext ctx, Target target){ BlockPos pos = target.getBlockPos(); return ctx.level().canSeeSky(pos); } }
