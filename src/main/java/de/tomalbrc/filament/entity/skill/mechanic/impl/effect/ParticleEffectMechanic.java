package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.Resolvable;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParticleEffectMechanic implements Mechanic {
    @SerializedName(value = "particle", alternate = {"p"})
    protected Resolvable<String> particle = Resolvable.literal("minecraft:flame"); // alias: p
    @SerializedName(value = "mob", alternate = {"m", "t"})
    protected Resolvable<ResourceLocation> mob; // alias: m, t (not implemented here)
    @SerializedName(value = "amount", alternate = {"a"})
    protected Resolvable<Integer> amount = Resolvable.literal(10); // alias: count, a
    @SerializedName(value = "spread", alternate = {"offset"})
    protected Resolvable<Double> spread = Resolvable.literal(0.0); // alias: offset
    @SerializedName(value = "hspread", alternate = {"hs"})
    protected Resolvable<Double> hSpread; // alias: hs
    @SerializedName(value = "vspread", alternate = {"vs", "yspread", "ys"})
    protected Resolvable<Double> vSpread; // alias: vs, yspread, ys
    @SerializedName(value = "xspread", alternate = {"xs"})
    protected Resolvable<Double> xSpread; // alias: xs
    @SerializedName(value = "zspread", alternate = {"zs"})
    protected Resolvable<Double> zSpread; // alias: zs
    @SerializedName(value = "speed", alternate = {"s"})
    protected Resolvable<Double> speed = Resolvable.literal(0.0); // alias: s
    @SerializedName(value = "yoffset", alternate = {"y"})
    protected Resolvable<Double> yOffset = Resolvable.literal(0.0); // alias: y
    @SerializedName(value = "viewdistance", alternate = {"vd"})
    protected Resolvable<Double> viewDistance = Resolvable.literal(128.); // alias: vd
    @SerializedName(value = "fromorigin", alternate = {"fo"})
    protected Resolvable<Boolean> fromOrigin = Resolvable.literal(false); // alias: fo
    @SerializedName(value = "directional", alternate = {"d"})
    protected Resolvable<Boolean> directional = Resolvable.literal(false); // alias: d
    @SerializedName(value = "directionreversed", alternate = {"dr"})
    protected Resolvable<Boolean> directionReversed = Resolvable.literal(false); // alias: dr
    @SerializedName(value = "direction", alternate = {"dir"})
    protected Resolvable<Vec3> direction = Resolvable.literal(Vec3.ZERO); // alias: dir
    @SerializedName(value = "fixedyaw", alternate = {"yaw"})
    protected Resolvable<Double> fixedYaw = Resolvable.literal(-1111.); // alias: yaw
    @SerializedName(value = "fixedpitch", alternate = {"pitch"})
    protected Resolvable<Double> fixedPitch = Resolvable.literal(-1111.); // alias: pitch

    public static TagParser<?> VALUE_PARSER = TagParser.create(NbtOps.INSTANCE);

    @Override
    public ExecutionResult execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets == null || targets.isEmpty()) return ExecutionResult.NULL;

        ServerLevel level = (ServerLevel) tree.level();

        // read particleOptions from particle string (like "minecraft:flame" or "minecraft:dust 1 0 0 1")
        ParticleOptions particleOptions;
        try {
            particleOptions = readParticle(new StringReader(particle.resolve(tree)), Filament.SERVER.registryAccess());
        } catch (CommandSyntaxException e) {
            // invalid particle
            Filament.LOGGER.warn("Invalid particle spec '{}' for particle mechanic", particle, e);
            return ExecutionResult.NULL;
        }

        if (particleOptions == null) return ExecutionResult.NULL;

        double effH = (hSpread.resolve(tree) != null ? hSpread.resolve(tree) : spread.resolve(tree));
        double effV = (vSpread.resolve(tree) != null ? vSpread.resolve(tree) : spread.resolve(tree));
        double effX = (xSpread.resolve(tree) != null ? xSpread.resolve(tree) : effH);
        double effZ = (zSpread.resolve(tree) != null ? zSpread.resolve(tree) : effH);

        var vd = viewDistance.resolve(tree);
        Collection<ServerPlayer> viewers = level.players().stream()
                .filter(p -> {
                    if (tree.caster() == null) return true;
                    double dist2 = p.position().distanceToSqr(tree.caster().position());
                    return dist2 <= vd * vd;
                })
                .toList();

        for (Target target : targets) {
            List<Vec3> positions = computePositions(tree, target, effX, effV, effZ);
            spawnPositionsForTarget(tree, target, positions, viewers, level, particleOptions);
        }

        return ExecutionResult.NULL;
    }

    protected List<Vec3> computePositions(SkillTree tree, Target target, double effX, double effV, double effZ) {
        Vec3 origin = fromOrigin.resolve(tree) && tree.origin() != null ? tree.origin() : target.getPosition();
        origin = origin.add(0, yOffset.resolve(tree), 0);

        List<Vec3> positions = new ArrayList<>(Math.max(1, amount.resolve(tree)));
        if (directional.resolve(tree)) {
            positions.add(origin);
            return positions;
        }

        for (int i = 0; i < Math.max(1, amount.resolve(tree)); i++) {
            Vec3 p = origin.add(randomOffset(effX), randomOffset(effV), randomOffset(effZ));
            if (fixedYaw.resolve(tree) != -1111 || fixedPitch.resolve(tree) != -1111) {
                p = applyFixedRotation(origin, p, fixedYaw.resolve(tree), fixedPitch.resolve(tree));
            }
            positions.add(p);
        }

        return positions;
    }

    protected int spawnPositionsForTarget(SkillTree tree, Target target, List<Vec3> positions,
                                          Collection<ServerPlayer> viewers, ServerLevel level,
                                          ParticleOptions particleOptions) {
        int successCount = 0;

        boolean isDirectional = directional.resolve(tree);
        Vec3 vel = isDirectional ? (directionReversed.resolve(tree) ? direction.resolve(tree).reverse() : direction.resolve(tree)) : Vec3.ZERO;

        for (Vec3 pos : positions) {
            for (ServerPlayer viewer : viewers) {
                boolean sent = level.sendParticles(
                        viewer,
                        particleOptions,
                        false, // force
                        false, // long distance
                        pos.x, pos.y, pos.z,
                        0,
                        vel.x,
                        vel.y,
                        vel.z,
                        speed.resolve(tree)
                );
                if (sent) successCount++;
            }
        }

        return successCount;
    }

    private double randomOffset(double spread) {
        return (Math.random() * 2.0 - 1.0) * spread;
    }

    Vec3 applyFixedRotation(Vec3 origin, Vec3 pos, double yawDeg, double pitchDeg) {
        Vec3 offset = pos.subtract(origin);

        if (yawDeg != -1111) {
            double yawRad = Math.toRadians(yawDeg);
            double x = offset.x * Math.cos(yawRad) - offset.z * Math.sin(yawRad);
            double z = offset.x * Math.sin(yawRad) + offset.z * Math.cos(yawRad);
            offset = new Vec3(x, offset.y, z);
        }

        if (pitchDeg != -1111) {
            double pitchRad = Math.toRadians(pitchDeg);
            double y = offset.y * Math.cos(pitchRad) - offset.z * Math.sin(pitchRad);
            double z = offset.y * Math.sin(pitchRad) + offset.z * Math.cos(pitchRad);
            offset = new Vec3(offset.x, y, z);
        }

        return origin.add(offset);
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_EFFECT;
    }

    protected static <T extends ParticleOptions, O> T readParticle(TagParser<O> parser, StringReader reader, ParticleType<T> particleType, HolderLookup.Provider registries) throws CommandSyntaxException {
        RegistryOps<O> registryOps = registries.createSerializationContext(parser.getOps());
        O object;
        if (reader.canRead() && reader.peek() == '{') {
            object = parser.parseAsArgument(reader);
        } else {
            object = registryOps.emptyMap();
        }

        DataResult<T> dataResult = particleType.codec().codec().parse(registryOps, object);
        return dataResult.getOrThrow();
    }

    protected static ParticleType<?> readParticleType(StringReader reader, HolderLookup<ParticleType<?>> particleTypeLookup) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        ResourceKey<ParticleType<?>> resourceKey = ResourceKey.create(Registries.PARTICLE_TYPE, resourceLocation);
        return particleTypeLookup.get(resourceKey).orElseThrow().value();
    }

    public static ParticleOptions readParticle(StringReader reader, HolderLookup.Provider registries) throws CommandSyntaxException {
        ParticleType<?> particleType = readParticleType(reader, registries.lookupOrThrow(Registries.PARTICLE_TYPE));
        return readParticle((TagParser<?>) VALUE_PARSER, reader, (ParticleType<?>) particleType, registries);
    }
}
