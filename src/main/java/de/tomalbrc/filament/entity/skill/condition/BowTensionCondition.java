package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

// TODO
class BowTensionCondition implements Condition {
    private final float min, max;
    BowTensionCondition(float min, float max) { this.min=min; this.max=max; }
    public boolean test(SkillContext ctx, LivingEntity target) { return true; }
}
