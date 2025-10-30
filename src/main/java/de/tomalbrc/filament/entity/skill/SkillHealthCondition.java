package de.tomalbrc.filament.entity.skill;

import net.minecraft.world.entity.LivingEntity;

public record SkillHealthCondition(double minPercent, double maxPercent) {
    public boolean isMet(LivingEntity entity) {
        double health = entity.getHealth();
        double max = entity.getMaxHealth();
        double pct = health / max;
        return pct >= minPercent && pct <= maxPercent;
    }
}