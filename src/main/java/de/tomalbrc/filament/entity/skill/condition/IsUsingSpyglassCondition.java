package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class IsUsingSpyglassCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof Player p)) return false;
        return p.isScoping();
    }
}
