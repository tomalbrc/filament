package de.tomalbrc.filament.entity;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.data.EntityData;
import de.tomalbrc.filament.registry.EntityRegistry;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class FilamentMob extends Animal implements PolymerEntity {
    EntityData data;

    @SuppressWarnings("unchecked")
    public FilamentMob(EntityType<? extends Entity> entityType, Level level) {
        super((EntityType<? extends Animal>) entityType, level);
        this.data = getData();
        this.xpReward = data.properties().xpReward;
        registerGoals();

        this.setInvulnerable(data.properties().invulnerable);
        this.noPhysics = data.properties().noPhysics;

        var movement = data.movement();
        if (movement.movementType != null)
            this.moveControl = movement.movementType.getControl(this);

        if (movement.jumpType != null)
            this.jumpControl = movement.jumpType.getControl(this);

        for (Map.Entry<PathType, Float> entry : movement.pathfindingMalus.entrySet()) {
            this.setPathfindingMalus(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected @NonNull PathNavigation createNavigation(@NonNull Level level) {
        return this.data.movement().navigationType.get(this, level);
    }

    public EntityData getData() {
        return EntityRegistry.getData(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        if (data != null) {
            for (var behaviour : data.goals()) {
                if (behaviour instanceof EntityBehaviour<?> entityBehaviour) {
                    entityBehaviour.registerGoals(this);
                }
            }
        }
    }

    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public GoalSelector getTargetSelector() {
        return this.targetSelector;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return BuiltInRegistries.ENTITY_TYPE.getValue(getData().entityType());
    }

    protected void updateNoActionTime() {
        float f = this.getLightLevelDependentMagicValue();
        if (f > 0.5F) {
            this.noActionTime += 2;
        }
    }

    @Override
    public float getWalkTargetValue(@NonNull BlockPos blockPos, @NonNull LevelReader levelReader) {
        if (data.properties().category == MobCategory.MONSTER) return -levelReader.getPathfindingCostFromLightLevels(blockPos);
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NonNull ServerLevel serverLevel, @NonNull AgeableMob ageableMob) {
        var id = data.properties().offspring;
        return (AgeableMob) BuiltInRegistries.ENTITY_TYPE.getValue(id).create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public void aiStep() {
        if (data.properties().category == MobCategory.MONSTER) this.updateNoActionTime();

        if (this.isAlive()) {
            boolean burn = data.properties().isSunSensitive && this.isSunBurnTick();
            if (burn) {
                ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageableItem()) {
                        Item item = itemStack.getItem();
                        itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
                        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                            this.onEquippedItemBroken(item, EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }

                    burn = false;
                }

                if (burn) {
                    this.igniteForSeconds(8.0F);
                }
            }
        }

        super.aiStep();
    }

    @Override
    public void customServerAiStep(@NonNull ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        if (this.forcedAgeTimer > 0) {
            if (this.forcedAgeTimer % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0, 0.0, 0.0, 0.0, 0.0);
            }

            --this.forcedAgeTimer;
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return this.data.properties().canPickupLoot;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, @NonNull Entity entity) {
        serverLevel.broadcastEntityEvent(this, (byte)4);

        return super.doHurtTarget(serverLevel, entity);
    }

    @Override
    @NotNull
    protected SoundEvent getSwimSound() {
        if (data.properties().sounds != null && data.properties().sounds.swim() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.swim());
        return super.getSwimSound();
    }

    @Override
    @NotNull
    protected SoundEvent getSwimSplashSound() {
        if (data.properties().sounds != null && data.properties().sounds.swimSplash() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.swimSplash());
        return super.getSwimSplashSound();
    }

    @Override
    protected SoundEvent getHurtSound(@NonNull DamageSource damageSource) {
        if (data.properties().sounds != null && data.properties().sounds.hurt() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.hurt());
        return super.getHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (data.properties().sounds != null && data.properties().sounds.death() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.death());
        return super.getDeathSound();
    }

    @Override
    @NotNull
    public LivingEntity.Fallsounds getFallSounds() {
        if (data.properties().sounds != null && data.properties().sounds.fall() != null)
            return new LivingEntity.Fallsounds(SoundEvent.createVariableRangeEvent(data.properties().sounds.fall().small()), SoundEvent.createVariableRangeEvent(data.properties().sounds.fall().big()));
        return super.getFallSounds();
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (data.properties().sounds != null && data.properties().sounds.ambient() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.ambient());
        return super.getAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return data.properties().ambientSoundInterval;
    }

    @Override
    public boolean isFood(@NonNull ItemStack itemStack) {
        var food = data.properties().food;
        if (food != null) {
            for (Identifier Identifier : food) {
                if (itemStack.is(BuiltInRegistries.ITEM.getValue(Identifier)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void setInLove(@Nullable Player player) {
        if (this.level() instanceof ServerLevel level) {
            for (int i = 0; i < 7; ++i) {
                double xOffset = this.random.nextGaussian() * 0.02;
                double yOffset = this.random.nextGaussian() * 0.02;
                double zOffset = this.random.nextGaussian() * 0.02;
                level.sendParticles(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0, xOffset, yOffset, zOffset, 0);
            }
        }

        super.setInLove(player);
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        if (!data.properties().canUsePortal) {
            return false;
        }

        return super.canUsePortal(bl);
    }

    @Override
    public void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {
        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MAX_HEALTH, 1.0, List.of()));
    }

    @Override
    public boolean canBeLeashed() {
        return data.properties().canBeLeashed;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return data.properties().despawnWhenFarAway && !this.isLeashed() && !this.hasCustomName();
    }

    @Override
    public boolean canMate(@NonNull Animal animal) {
        if (animal == this) {
            return false;
        } else if (animal.getType() != this.getType()) {
            return false;
        } else {
            return this.isInLove() && animal.isInLove();
        }
    }


}
