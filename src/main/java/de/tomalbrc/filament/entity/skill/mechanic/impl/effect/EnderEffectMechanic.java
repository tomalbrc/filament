package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.util.TextUtil;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.LevelEvent;

import java.util.List;

public class EnderEffectMechanic implements Mechanic {
    @Override
    public int execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets != null) {
            for (Target target : targets) {
                tree.level().levelEvent(LevelEvent.PARTICLES_EYE_OF_ENDER_DEATH, target.getBlockPos(), 0);
            }
        }

        return 0;
    }


    @Override
    public ResourceLocation id() {
        return Mechanics.ENDER_EFFECT;
    }
}