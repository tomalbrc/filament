package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import de.tomalbrc.filament.util.TextUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class MessageMechanic implements Mechanic {
    private final String message;

    public MessageMechanic(String message, Targeter targeter) {
        this.message = message;
    }

    @Override
    public int execute(SkillContext context) {
        List<Target> targets = context.targets();
        if (targets != null) {
            for (Target target : targets) {
                if (target.getEntity() instanceof ServerPlayer player) {
                    player.sendSystemMessage(TextUtil.formatText(message));
                }
            }
        }

        return 0;
    }


    @Override
    public ResourceLocation id() {
        return Mechanics.MESSAGE;
    }
}