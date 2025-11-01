package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

class IsInSurvivalModeCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        if (!(target.getEntity() instanceof ServerPlayer p)) return false;
        return p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
    }
}
