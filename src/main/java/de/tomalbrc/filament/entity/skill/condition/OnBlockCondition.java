package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

class OnBlockCondition implements Condition { public boolean test(SkillContext ctx, LivingEntity target){ BlockPos pos = target.getOnPos(); return !ctx.level().getBlockState(pos).isAir(); } }
