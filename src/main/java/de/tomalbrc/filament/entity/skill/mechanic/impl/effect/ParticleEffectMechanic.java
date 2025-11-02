package de.tomalbrc.filament.entity.skill.mechanic.impl.effect;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DataResult;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ParticleEffectMechanic implements Mechanic {
    private String particle = "dust"; // alias: p
    private ResourceLocation mob; // alias: m, t (entity-based particles, not yet implemented)
    private int amount = 10; // alias: count, a
    private double spread = 0; // alias: offset
    private Double hSpread; // alias: hs
    private Double vSpread; // alias: vs, yspread, ys
    private Double xSpread; // alias: xs
    private Double zSpread; // alias: zs
    private double speed = 0; // alias: s
    private double yOffset = 0; // alias: y
    private double viewDistance = 128; // alias: vd
    private boolean fromOrigin = false; // alias: fo
    private boolean directional = false; // alias: d
    private boolean directionReversed = false; // alias: dr
    private Vec3 direction = Vec3.ZERO; // alias: dir
    private double fixedYaw = -1111; // alias: yaw
    private double fixedPitch = -1111; // alias: pitch

    @Override
    public int execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets == null || targets.isEmpty()) return 0;

        ServerLevel level = (ServerLevel) tree.level();

        ParticleOptions particleOptions = null;
        try {
            particleOptions = readParticle(new StringReader(particle), Filament.SERVER.registryAccess());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        if (particleOptions == null) {
            return 0;
        }

        double effH = (hSpread != null ? hSpread : spread);
        double effV = (vSpread != null ? vSpread : spread);
        double effX = (xSpread != null ? xSpread : effH);
        double effZ = (zSpread != null ? zSpread : effH);

        Collection<ServerPlayer> viewers = level.players().stream()
                .filter(p -> tree.caster() == null || p.position().distanceToSqr(tree.caster().position()) <= viewDistance * viewDistance)
                .toList();

        for (Target target : targets) {
            Vec3 origin = fromOrigin && tree.origin() != null
                    ? tree.origin()
                    : target.getPosition();
            origin = origin.add(0, yOffset, 0);

            int particleAmount = directional ? 0 : amount;
            for (int i = 0; i < particleAmount; i++) {

                Vec3 pos;
                Vec3 velocity;

                if (directional) {
                    pos = origin;
                    velocity = directionReversed ? direction.reverse() : direction;
                } else {
                    pos = origin.add(
                            randomOffset(effX),
                            randomOffset(effV),
                            randomOffset(effZ)
                    );

                    if (fixedYaw != -1111 || fixedPitch != -1111) {
                        pos = applyFixedRotation(origin, pos, fixedYaw, fixedPitch);
                    }

                    velocity = Vec3.ZERO;
                }

                for (ServerPlayer viewer : viewers) {
                    level.sendParticles(
                            viewer,
                            particleOptions,
                            false, // force
                            false,
                            pos.x, pos.y, pos.z,
                            0,
                            velocity.x,
                            velocity.y,
                            velocity.z,
                            speed
                    );
                }
            }
        }

        return 0;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.PARTICLE_EFFECT;
    }

    private double randomOffset(double spread) {
        return (Math.random() * 2. - 1.) * spread;
    }

    private Vec3 applyFixedRotation(Vec3 origin, Vec3 pos, double yawDeg, double pitchDeg) {
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

    private static <T extends ParticleOptions, O> T readParticle(TagParser<O> parser, StringReader reader, ParticleType<T> particleType, HolderLookup.Provider registries) throws CommandSyntaxException {
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

    public static ParticleOptions readParticle(StringReader reader, HolderLookup.Provider registries) throws CommandSyntaxException {
        ParticleType<?> particleType = readParticleType(reader, registries.lookupOrThrow(Registries.PARTICLE_TYPE));
        return readParticle(VALUE_PARSER, reader, particleType, registries);
    }

    private static ParticleType<?> readParticleType(StringReader reader, HolderLookup<ParticleType<?>> particleTypeLookup) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        ResourceKey<ParticleType<?>> resourceKey = ResourceKey.create(Registries.PARTICLE_TYPE, resourceLocation);
        return particleTypeLookup.get(resourceKey).orElseThrow().value();
    }

    public static TagParser<?> VALUE_PARSER = TagParser.create(NbtOps.INSTANCE);
}