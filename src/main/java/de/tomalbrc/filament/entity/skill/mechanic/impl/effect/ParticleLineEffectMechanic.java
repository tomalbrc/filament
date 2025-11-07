package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ParticleLineEffectMechanic extends ParticleEffectMechanic {
    @SerializedName(value = "distancebetween", alternate = {"db"})
    protected double distanceBetween = 0.25;

    @Override
    protected List<Vec3> computePositions(SkillTree tree, Target target, double effX, double effV, double effZ) {
        Vec3 origin = (fromOrigin.resolve(tree) && tree.origin() != null) ? tree.origin() : tree.caster.position();
        origin = origin.add(0, yOffset.resolve(tree), 0);

        Vec3 other = target.getPosition().add(0, yOffset.resolve(tree), 0);

        Vec3 dir = other.subtract(origin);
        double length = dir.length();
        if (length == 0) {
            List<Vec3> single = new ArrayList<>(1);
            single.add(origin);
            return single;
        }
        Vec3 unit = dir.scale(1.0 / length);

        List<Vec3> positions = new ArrayList<>((int) Math.max(1, Math.ceil(length / Math.max(1e-6, distanceBetween))));
        double step = Math.max(1e-6, distanceBetween);
        for (double d = 0; d <= length; d += step) {
            Vec3 p = origin.add(unit.scale(d));
            if (fixedYaw.resolve(tree) != -1111 || fixedPitch.resolve(tree) != -1111) {
                p = applyFixedRotation(origin, p, fixedYaw.resolve(tree), fixedPitch.resolve(tree));
            }
            positions.add(p);
        }
        return positions;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_LINE_EFFECT;
    }
}
