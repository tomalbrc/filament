package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.RuntimeTypeAdapterFactoryWithAliases;
import de.tomalbrc.filament.entity.skill.mechanic.impl.*;
import de.tomalbrc.filament.entity.skill.mechanic.impl.effect.*;
import de.tomalbrc.filament.util.Util;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Mechanics {
    public static RuntimeTypeAdapterFactoryWithAliases<Mechanic> TYPE_ADAPTER_FACTORY = RuntimeTypeAdapterFactoryWithAliases.of(Mechanic.class, "type");

    public static ResourceLocation register(ResourceLocation id, Class<? extends Mechanic> type) {
        TYPE_ADAPTER_FACTORY.registerSubtype(type, id.getPath());
        return id;
    }

    public static ResourceLocation register(ResourceLocation id, Class<? extends Mechanic> type, String... aliases) {
        TYPE_ADAPTER_FACTORY.registerSubtypeWithAliases(type, id.getPath(), aliases);
        return id;
    }

    public static ResourceLocation DELAY = register(Util.id("delay"), DelayMechanic.class);
    public static ResourceLocation MESSAGE = register(Util.id("message"), MessageMechanic.class);
    public static ResourceLocation POTION = register(Util.id("potion"), PotionMechanic.class);
    public static ResourceLocation SKILL = register(Util.id("skill"), SkillMechanic.class);
    public static ResourceLocation SET_VARIABLE = register(Util.id("set_variable"), SetVariableMechanic.class);
    public static ResourceLocation SOUND = register(Util.id("sound"), SoundMechanic.class);
    public static ResourceLocation ENDER_EFFECT = register(Util.id("ender_effect"), EnderEffectMechanic.class, "ender", "effect:ender", "e:ender");
    public static ResourceLocation PARTICLE_EFFECT = register(Util.id("particle"), ParticleEffectMechanic.class);
    public static ResourceLocation PARTICLE_RING_EFFECT = register(Util.id("particle_ring"), ParticleRingEffectMechanic.class);
    public static ResourceLocation PARTICLE_LINE_EFFECT = register(Util.id("particle_line"), ParticleLineEffectMechanic.class);
    public static ResourceLocation PARTICLE_LINE_HELIX = register(Util.id("particle_line_helix"), ParticleLineHelix.class, "ParticleLineHelix", "effect:particlelinehelix", "particlehelixline");
    public static ResourceLocation PARTICLE_SPHERE = register(Util.id("particle_sphere"), ParticleSphereEffectMechanic.class, "effect:particlesphere", "e:ps", "ps");

    public static ResourceLocation FEED = register(Util.id("feed"), FeedMechanic.class);
    public static ResourceLocation IGNITE = register(Util.id("ignite"), IgniteMechanic.class);
}
