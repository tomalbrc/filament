package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.util.TextUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class MessageMechanic implements Mechanic {
    private final String message;

    public MessageMechanic(String message) {
        this.message = message;
    }

    @Override
    public ExecutionResult execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets != null) {
            for (Target target : targets) {
                if (target.getEntity() instanceof ServerPlayer player) {
                    player.sendSystemMessage(TextUtil.formatText(message));
                }
            }
        }

        return ExecutionResult.NULL;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.MESSAGE;
    }
}