package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

public class HealthPercentageCondition implements Condition {
    private final double minPct; // 0.0..1.0
    private final double maxPct; // 0.0..1.0

    public HealthPercentageCondition(double minPct, double maxPct) {
        this.minPct = minPct;
        this.maxPct = maxPct;
    }

    @Override
    public boolean test(SkillContext context, Target target) {
        LivingEntity e = context.caster().asLivingEntity();
        assert e != null;
        double pct = e.getHealth() / e.getMaxHealth();
        return pct >= minPct && pct <= maxPct;
    }
}
