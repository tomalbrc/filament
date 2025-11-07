package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ParticleRingEffectMechanic extends ParticleEffectMechanic {
    @SerializedName(value = "radius", alternate = {"r"})
    protected double radius = 1.0;
    @SerializedName(value = "points", alternate = {"count"})
    protected int points = 24;

    @Override
    protected List<Vec3> computePositions(SkillTree tree, Target target, double effX, double effV, double effZ) {
        Vec3 origin = (fromOrigin.resolve(tree) && tree.origin() != null) ? tree.origin() : target.getPosition();
        origin = origin.add(0, yOffset.resolve(tree), 0);

        List<Vec3> positions = new ArrayList<>(Math.max(1, points));
        for (int i = 0; i < Math.max(1, points); i++) {
            double angle = 2.0 * Math.PI * ((double) i / (double) points);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Vec3 p = origin.add(x, 0, z);
            if (fixedYaw.resolve(tree) != -1111 || fixedPitch.resolve(tree) != -1111) {
                p = applyFixedRotation(origin, p, fixedYaw.resolve(tree), fixedPitch.resolve(tree));
            }
            positions.add(p);
        }
        return positions;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_RING_EFFECT;
    }
}
