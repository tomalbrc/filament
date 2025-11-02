package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;

public class FoodSaturationCondition implements Condition {
    private final float min, max;

    FoodSaturationCondition(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        return p.getFoodData().getSaturationLevel() >= min && p.getFoodData().getSaturationLevel() <= max;
    }
}
