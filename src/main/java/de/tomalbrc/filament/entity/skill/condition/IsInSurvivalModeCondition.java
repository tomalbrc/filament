package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;

class IsInSurvivalModeCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof ServerPlayer p)) return false;
        return p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
    }
}
