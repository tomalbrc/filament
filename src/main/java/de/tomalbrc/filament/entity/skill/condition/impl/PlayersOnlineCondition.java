package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class PlayersOnlineCondition implements Condition {
    private final int min, max;

    PlayersOnlineCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        int c = ctx.level().getServer().getPlayerList().getPlayers().size();
        return c >= min && c <= max;
    }
}
