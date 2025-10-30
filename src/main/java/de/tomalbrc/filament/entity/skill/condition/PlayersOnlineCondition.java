package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class PlayersOnlineCondition implements Condition {
    private final int min, max;

    PlayersOnlineCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        int c = ctx.level().getServer().getPlayerList().getPlayers().size();
        return c >= min && c <= max;
    }
}
