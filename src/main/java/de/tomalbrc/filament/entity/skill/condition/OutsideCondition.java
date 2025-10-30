package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

class OutsideCondition implements Condition { public boolean test(SkillContext ctx, LivingEntity target){ BlockPos pos = target.blockPosition(); return ctx.level().canSeeSky(pos); } }
