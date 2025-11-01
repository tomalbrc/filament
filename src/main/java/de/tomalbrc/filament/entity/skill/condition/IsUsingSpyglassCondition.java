package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;

class IsUsingSpyglassCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        return p.isScoping();
    }
}
