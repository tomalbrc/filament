package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.RuntimeTypeAdapterFactoryWithAliases;
import de.tomalbrc.filament.entity.skill.target.impl.*;
import de.tomalbrc.filament.util.Util;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Targeters {
    public static RuntimeTypeAdapterFactoryWithAliases<Targeter> TYPE_ADAPTER_FACTORY = RuntimeTypeAdapterFactoryWithAliases.of(Targeter.class, "type");

    public static ResourceLocation register(ResourceLocation id, Class<? extends Targeter> type) {
        TYPE_ADAPTER_FACTORY.registerSubtypeWithAliases(type, id.getPath(), id.getPath());
        return id;
    }

    public static ResourceLocation register(ResourceLocation id, Class<? extends Targeter> type, String... aliases) {
        TYPE_ADAPTER_FACTORY.registerSubtypeWithAliases(type, id.getPath(), aliases);
        return id;
    }

    public static final ResourceLocation TARGET = register(Util.id("target"), TargetTargeter.class, "T", "Target");
    public static final ResourceLocation TRIGGER = register(Util.id("trigger"), TriggerTargeter.class, "Trigger");
    public static final ResourceLocation VEHICLE = register(Util.id("vehicle"), VehicleTargeter.class, "Vehicle");
    public static final ResourceLocation SELF = register(Util.id("self"), SelfTargeter.class, "Self", "Caster", "Boss", "Mob");
    public static final ResourceLocation SELF_LOCATION = register(Util.id("self_location"), SelfLocationTargeter.class, "SelfLocation", "casterLocation", "bossLocation", "mobLocation");
    public static final ResourceLocation SELF_EYE_LOCATION = register(Util.id("self_eye_location"), SelfEyeLocationTargeter.class, "SelfEyeLocation", "eyeDirection");
    public static final ResourceLocation PLAYERS_IN_RADIUS = register(Util.id("players_in_radius"), PlayersInRadiusTargeter.class, "PlayersInRadius", "PIR");
    public static final ResourceLocation PLAYERS_IN_RING = register(Util.id("players_in_ring"), PlayersInRingTargeter.class, "PlayersInRing");
    public static final ResourceLocation PLAYERS_IN_WORLD = register(Util.id("players_in_world"), PlayersInWorldTargeter.class, "world", "PlayersInWorld");
    public static final ResourceLocation TRACKED = register(Util.id("tracked_players"), TrackedTargeter.class, "tracked", "TrackedPlayers");
    public static final ResourceLocation OWNER = register(Util.id("owner"), OwnerTargeter.class, "Owner");
    public static final ResourceLocation MOBS_IN_RADIUS = register(Util.id("mobs_in_radius"), MobsInRadiusTargeter.class, "MobsInRadius", "MIR");
    public static final ResourceLocation ITEMS_IN_RADIUS = register(Util.id("items_in_radius"), ItemsInRadiusTargeter.class, "ItemsInRadius", "IIR");
    public static final ResourceLocation ENTITIES_IN_RADIUS = register(Util.id("entities_in_radius"), EntitiesInRadiusTargeter.class, "EntitiesInRadius", "livingEntitiesInRadius", "livingInRadius", "allInRadius", "EIR");
    public static final ResourceLocation FORWARD = register(Util.id("forward"), ForwardTargeter.class, "Forward");
}
