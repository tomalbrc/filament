package de.tomalbrc.filament.entity;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.filament.registry.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class AnimatedFilamentEnemyMob extends FilamentEnemyMob implements AnimatedEntity {
    private final LivingEntityHolder<AnimatedFilamentEnemyMob> holder;

    public AnimatedFilamentEnemyMob(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);

        this.holder = new LivingEntityHolder<>(this, ModelRegistry.getModel(Objects.requireNonNull(data.animation()).model()));
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder, data.animation());
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }
}
