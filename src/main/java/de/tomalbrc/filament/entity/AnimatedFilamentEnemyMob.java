package de.tomalbrc.filament.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AnimatedFilamentEnemyMob extends FilamentEnemyMob {
    public AnimatedFilamentEnemyMob(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }
}
