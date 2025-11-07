package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ParticleLineHelix extends ParticleEffectMechanic {
    @SerializedName(value = "distanceBetween", alternate = {"db"})
    protected double distanceBetween = 1.0;
    @SerializedName(value = "startYOffset", alternate = {"syo", "ystartoffset"})
    protected double startYOffset = 0.0;
    @SerializedName(value = "targetYOffset", alternate = {"tyo", "ytargetoffset"})
    protected double targetYOffset = 0.0;
    @SerializedName(value = "helixlength", alternate = {"hl"})
    protected double helixLength = 2.0;
    @SerializedName(value = "helixradius", alternate = {"hr"})
    protected double helixRadius = 1.0;
    @SerializedName(value = "helixrotation", alternate = {"rot"})
    protected double helixRotation = 0.0;
    @SerializedName(value = "maxdistance", alternate = {"md"})
    protected double maxDistance = 256.0;

    @Override
    protected List<Vec3> computePositions(SkillTree tree, Target target, double effX, double effV, double effZ) {
        Vec3 start;
        if (fromOrigin.resolve(tree) && tree.origin() != null) {
            start = tree.origin();
        } else if (tree.caster() != null) {
            start = tree.caster().position();
        } else {
            start = target.getPosition();
        }

        start = start.add(0.0, startYOffset, 0.0);
        Vec3 end = target.getPosition().add(0.0, targetYOffset, 0.0);

        Vec3 axis = end.subtract(start);
        double fullLength = axis.length();

        if (fullLength <= 1e-9) {
            return circularRingAt(tree, start);
        }

        double length = Math.min(fullLength, maxDistance);
        Vec3 axisUnit = axis.scale(1.0 / fullLength); // unit along true axis

        double step = Math.max(1e-6, distanceBetween);
        int points = Math.max(1, (int) Math.ceil(length / step));

        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        if (Math.abs(axisUnit.dot(up)) > 0.9999) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 u = axisUnit.cross(up);
        double uLen = u.length();
        if (uLen < 1e-9) {
            u = new Vec3(1.0, 0.0, 0.0);
        } else {
            u = u.scale(1.0 / uLen);
        }
        Vec3 v = axisUnit.cross(u);

        double initialAngle = Math.toRadians(helixRotation);

        List<Vec3> positions = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            double d = (points == 1) ? 0.0 : ((double) i / (double) (points - 1)) * length;
            Vec3 along = start.add(axisUnit.scale(d));

            double t = (length <= 0) ? 0.0 : (d / length);
            double angle = initialAngle + t * helixLength * 2.0 * Math.PI;

            double cos = Math.cos(angle) * helixRadius;
            double sin = Math.sin(angle) * helixRadius;
            Vec3 offset = u.scale(cos).add(v.scale(sin));

            Vec3 p = along.add(offset);

            if (fixedYaw.resolve(tree) != -1111 || fixedPitch.resolve(tree) != -1111) {
                p = applyFixedRotation(start, p, fixedYaw.resolve(tree), fixedPitch.resolve(tree));
            }

            positions.add(p);
        }

        return positions;
    }

    private List<Vec3> circularRingAt(SkillTree tree, Vec3 center) {
        int points = Math.max(8, (int) Math.ceil((helixLength * 2.0 * Math.PI * helixRadius) / Math.max(1e-6, distanceBetween)));
        List<Vec3> positions = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            double frac = (double) i / (double) points;
            double angle = initialRotationRad() + frac * helixLength * 2.0 * Math.PI;
            double x = Math.cos(angle) * helixRadius;
            double z = Math.sin(angle) * helixRadius;
            Vec3 p = center.add(x, 0.0, z);
            if (fixedYaw.resolve(tree) != -1111 || fixedPitch.resolve(tree) != -1111) {
                p = applyFixedRotation(center, p, fixedYaw.resolve(tree), fixedPitch.resolve(tree));
            }
            positions.add(p);
        }
        return positions;
    }

    private double initialRotationRad() {
        return Math.toRadians(helixRotation);
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_LINE_HELIX;
    }
}
