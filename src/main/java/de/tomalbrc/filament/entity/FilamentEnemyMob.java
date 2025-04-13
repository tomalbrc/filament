package de.tomalbrc.filament.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class FilamentEnemyMob extends FilamentMob implements Enemy {
    public FilamentEnemyMob(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }
}
