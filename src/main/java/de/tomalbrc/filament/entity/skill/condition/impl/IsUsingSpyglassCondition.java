package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;

public class IsUsingSpyglassCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        return p.isScoping();
    }
}
