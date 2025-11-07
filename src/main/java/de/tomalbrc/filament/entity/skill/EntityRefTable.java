package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.Filament;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityRefTable {
    private final Map<EntityReference<?>, Double> damagePerEntity = new HashMap<>();

    public <E extends Entity> double add(E entity, double damage) {
        var ref = EntityReference.of(entity);
        return damagePerEntity.merge(ref, damage, Double::sum);
    }

    public double add(UUID entity, double damage) {
        var ref = EntityReference.of(entity);
        return damagePerEntity.merge(ref, damage, Double::sum);
    }

    public <E extends Entity> void set(E entity, double damage) {
        var ref = EntityReference.of(entity);
        damagePerEntity.put(ref, damage);
    }

    public <E extends Entity> double get(E entity) {
        var ref = EntityReference.of(entity);
        return damagePerEntity.getOrDefault(ref, 0.);
    }

    public void set(UUID entity, double damage) {
        var ref = EntityReference.of(entity);
        damagePerEntity.put(ref, damage);
    }

    public double get(UUID entity) {
        var ref = EntityReference.of(entity);
        return damagePerEntity.getOrDefault(ref, 0.);
    }

    public Collection<Entity> descending(Level level) {
        return damagePerEntity.entrySet()
                        .stream()
                        .sorted(Map.Entry.<EntityReference<?>, Double>comparingByValue().reversed())
                        .<EntityReference<?>>map(Map.Entry::getKey)
                        .map(x -> EntityReference.getEntity((EntityReference<Entity>) x, level))
                        .toList();
    }

    public Collection<Entity> ascending(Level level) {
        return damagePerEntity.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .<EntityReference<?>>map(Map.Entry::getKey)
                .map(x -> EntityReference.getEntity((EntityReference<Entity>) x, level))
                .toList();
    }

    public Collection<ServerPlayer> players() {
        return Filament.SERVER.getPlayerList().getPlayers().stream().filter(x -> damagePerEntity.containsKey(EntityReference.of(x))).toList();
    }

}