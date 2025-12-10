package de.tomalbrc.filament.data.properties;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class EntityProperties {
    public @Nullable List<Float> size;
    public @Nullable MobCategory category;
    public @Nullable Sounds sounds;
    public @Nullable Set<Identifier> food;
    public @Nullable Identifier offspring;
    public int ambientSoundInterval = 80;
    public int xpReward = 5;
    public boolean isSunSensitive = false;
    public boolean canPickupLoot = false;
    public boolean shouldDespawnInPeaceful = true;
    public boolean invulnerable = false;
    public boolean fireImmune = false;
    public boolean noPhysics = false;
    public boolean noSave = false;
    public boolean noSummon = false;
    public boolean canUsePortal = false;
    public boolean canBeLeashed = false;
    public boolean despawnWhenFarAway = false;
    public boolean forceEnemy = false;

    public record FallSounds(Identifier small, Identifier big) {

    }
    public record Sounds(Identifier ambient, Identifier swim, Identifier swimSplash, Identifier hurt, Identifier death, FallSounds fall) {

    }
}
