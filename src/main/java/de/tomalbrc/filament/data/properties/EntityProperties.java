package de.tomalbrc.filament.data.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityProperties {
    public static final EntityProperties EMPTY = new EntityProperties();

    public @Nullable List<Float> size;
    public @Nullable MobCategory category;
    public @Nullable Sounds sounds;
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

    public record FallSounds(ResourceLocation small, ResourceLocation big) {

    }
    public record Sounds(ResourceLocation ambient, ResourceLocation swim, ResourceLocation swimSplash, ResourceLocation hurt, ResourceLocation death, FallSounds fall) {

    }
}
