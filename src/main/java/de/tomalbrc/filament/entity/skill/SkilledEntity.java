package de.tomalbrc.filament.entity.skill;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public interface SkilledEntity<T extends Entity & SkilledEntity<T>> {
    MobSkills<T> mobSkills();
    Map<String, Variable> getVariables();
    EntityRefTable threatTable();
    EntityRefTable immunityTable();
    LivingEntity getTarget();
    List<ServerPlayer> getTracking();
    ServerBossEvent bossEvent();

    float getHealth();
    float getMaxHealth();
    Vec3 position();
    double getX();
    double getY();
    double getZ();
    Level level();
    RandomSource getRandom();
    Entity getVehicle();
    List<Entity> getPassengers();
    LivingEntity getOwner();
    Vec3 getEyePosition();
}
