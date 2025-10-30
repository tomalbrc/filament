package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class FoodLevelCondition implements Condition {
    private final int min, max;

    FoodLevelCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof Player p)) return false;
        return p.getFoodData().getFoodLevel() >= min && p.getFoodData().getFoodLevel() <= max;
    }
}
