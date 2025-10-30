package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class EnchantingLevelCondition implements Condition {
    private final int min, max;

    EnchantingLevelCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof Player p)) return false;
        int lvl = p.experienceLevel;
        return lvl >= min && lvl <= max;
    }
}