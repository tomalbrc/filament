package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class PlayersOnlineCondition implements Condition {
    private final int min, max;

    PlayersOnlineCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        int c = ctx.level().getServer().getPlayerList().getPlayers().size();
        return c >= min && c <= max;
    }
}
