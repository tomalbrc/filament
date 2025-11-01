package de.tomalbrc.filament.entity.skill.target;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import de.tomalbrc.filament.util.Util;
import net.minecraft.resources.ResourceLocation;

public class Targeters {
    public static RuntimeTypeAdapterFactory<Targeter> TYPE_ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(Targeter.class, "type");

    public static ResourceLocation register(ResourceLocation id, Class<? extends Targeter> type) {
        TYPE_ADAPTER_FACTORY.registerSubtype(type, id.toString());
        return id;
    }

    public static final ResourceLocation TARGET = register(Util.id("target"), TargetTargeter.class);
    public static final ResourceLocation VEHICLE = register(Util.id("vehicle"), VehicleTargeter.class);
    public static final ResourceLocation SELF = register(Util.id("self"), SelfTargeter.class);
    public static final ResourceLocation PLAYERS_IN_RADIUS = register(Util.id("players_in_radius"), PlayersInRadiusTargeter.class);
    public static final ResourceLocation PLAYERS_IN_RING = register(Util.id("players_in_ring"), PlayersInRingTargeter.class);
    public static final ResourceLocation PLAYERS_IN_WORLD = register(Util.id("players_in_world"), PlayersInWorldTargeter.class);
    public static final ResourceLocation TRACKED = register(Util.id("tracked"), TrackedTargeter.class);
    public static final ResourceLocation OWNER = register(Util.id("owner"), OwnerTargeter.class);
    public static final ResourceLocation MOBS_IN_RADIUS = register(Util.id("mobs_in_radius"), MobsInRadiusTargeter.class);
    public static final ResourceLocation ITEMS_IN_RADIUS = register(Util.id("items_in_radius"), ItemsInRadiusTargeter.class);
    public static final ResourceLocation ENTITIES_IN_RADIUS = register(Util.id("entities_in_radius"), EntitiesInRadiusTargeter.class);
}
