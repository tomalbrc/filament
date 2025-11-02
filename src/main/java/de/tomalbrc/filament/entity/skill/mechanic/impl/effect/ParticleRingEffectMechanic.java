package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ParticleRingEffectMechanic implements Mechanic {
    @Override
    public int execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets != null) {
            for (Target target : targets) {
                ServerLevel level = (ServerLevel) tree.level();

                Vec3 pos = target.getPosition();
                //level.sendParticles()
                // TODO: impl
            }
        }

        return 0;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_RING_EFFECT;
    }
}