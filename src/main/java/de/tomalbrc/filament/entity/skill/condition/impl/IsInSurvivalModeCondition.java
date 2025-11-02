package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class IsInSurvivalModeCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof ServerPlayer p)) return false;
        return p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
    }
}
