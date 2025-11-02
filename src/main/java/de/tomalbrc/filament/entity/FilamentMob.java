package de.tomalbrc.filament.entity;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.data.entity.EntityData;
import de.tomalbrc.filament.entity.skill.MobSkills;
import de.tomalbrc.filament.entity.skill.ThreatTable;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.registry.EntityRegistry;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilamentMob extends Animal implements OwnableEntity, PolymerEntity {
    final Map<String, Variable> variables = new Object2ObjectOpenHashMap<>();

    EntityData data;
    MobSkills mobSkills;
    ServerBossEvent bossEvent;
    EntityReference<LivingEntity> owner;
    boolean triggeredDeath = false;

    ThreatTable threatTable;
    ThreatTable threatTable;

    List<ServerPlayer> tracking = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public FilamentMob(EntityType<? extends Entity> entityType, Level level) {
        super((EntityType<? extends Animal>) entityType, level);
        this.data = getData();

        this.mobSkills = new MobSkills(this);

        this.xpReward = this.data.properties().xpReward;
        registerGoals();

        this.setInvulnerable(this.data.properties().invulnerable);
        this.noPhysics = this.data.properties().noPhysics;

        //this.setPathfindingMalus(PathType.BLOCKED);

        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);

        if (this.data.skills() != null)
            this.data.skills().forEach(this.mobSkills::add);
    }

    public EntityData getData() {
        return EntityRegistry.getData(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()));
    }

    public Map<String, Variable> getVariables() {
        return this.variables;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        if (this.data != null) {
            for (var behaviour : this.data.goals()) {
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
        float forbiddenAlchemy = this.getLightLevelDependentMagicValue();
        if (forbiddenAlchemy > 0.5f) {
            this.noActionTime += 2;
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (data.properties().category == MobCategory.MONSTER) return -levelReader.getPathfindingCostFromLightLevels(blockPos);
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        var id = data.properties().offspring;
        return (AgeableMob) BuiltInRegistries.ENTITY_TYPE.getValue(id).create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public void aiStep() {
        if (this.data.properties().category == MobCategory.MONSTER) this.updateNoActionTime();

        if (this.isAlive()) {
            boolean burn = this.data.properties().isSunSensitive && this.isSunBurnTick();
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
    public void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        this.mobSkills.tick(serverLevel);

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
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        this.mobSkills.onAttack(serverLevel, entity);

        serverLevel.broadcastEntityEvent(this, (byte)4);

        return super.doHurtTarget(serverLevel, entity);
    }

    @Override
    @NotNull
    protected SoundEvent getSwimSound() {
        if (this.data.properties().sounds != null && this.data.properties().sounds.swim() != null)
            return SoundEvent.createVariableRangeEvent(this.data.properties().sounds.swim());
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
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (data.properties().sounds != null && data.properties().sounds.hurt() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.hurt());
        return super.getHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.data.properties().sounds != null && this.data.properties().sounds.death() != null)
            return SoundEvent.createVariableRangeEvent(data.properties().sounds.death());
        return super.getDeathSound();
    }

    @Override
    @NotNull
    public LivingEntity.Fallsounds getFallSounds() {
        if (this.data.properties().sounds != null && this.data.properties().sounds.fall() != null)
            return new LivingEntity.Fallsounds(SoundEvent.createVariableRangeEvent(this.data.properties().sounds.fall().small()), SoundEvent.createVariableRangeEvent(this.data.properties().sounds.fall().big()));
        return super.getFallSounds();
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.data.properties().sounds != null && this.data.properties().sounds.ambient() != null)
            return SoundEvent.createVariableRangeEvent(this.data.properties().sounds.ambient());
        return super.getAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return this.data.properties().ambientSoundInterval;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        var food = this.data.properties().food;
        if (food != null) {
            for (ResourceLocation resourceLocation : food) {
                if (itemStack.is(BuiltInRegistries.ITEM.getValue(resourceLocation)))
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
        if (!this.data.properties().canUsePortal) {
            return false;
        }

        return super.canUsePortal(bl);
    }

    @Override
    public void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {

        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MAX_HEALTH, 1.0, List.of()));
    }

    //@Override
    //public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        //BuiltInRegistries.ENTITY_TYPE.getValue(this.data.entityType());
    //}

    @Override
    public boolean canBeLeashed() {
        return this.data.properties().canBeLeashed;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return this.data.properties().despawnWhenFarAway && !this.isLeashed() && !this.hasCustomName();
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        } else if (animal.getType() != this.getType()) {
            return false;
        } else {
            return this.isInLove() && animal.isInLove();
        }
    }

    @Override
    public void finalizeSpawnChildFromBreeding(ServerLevel level, Animal animal, @Nullable AgeableMob baby) {
        this.mobSkills.onBreed(animal);
        super.finalizeSpawnChildFromBreeding(level, animal, baby);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.mobSkills.onSpawn();
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.mobSkills.onLoad();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void tickDeath() {
        if (!triggeredDeath) {
            triggeredDeath = true;
            this.mobSkills.onDeath();
        }
        super.tickDeath();
    }

    @Override
    public void checkDespawn() {
        var isRemoved = isRemoved();
        super.checkDespawn();
        if (isRemoved() != isRemoved && isRemoved) {
            this.mobSkills.onDespawn();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        this.mobSkills.onInteract(player, hand);
        return super.mobInteract(player, hand);
    }

    @Override
    public void setLastHurtByPlayer(Player player, int memoryTime) {
        super.setLastHurtByPlayer(player, memoryTime);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        this.mobSkills.onChangeTarget(target);
        super.setTarget(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        this.variables.put("lastDamageCause", new Variable(damageSource.typeHolder().getRegisteredName()));
        this.variables.put("damageTags", new Variable(damageSource.typeHolder().tags().map(TagKey::location).collect(Collectors.toSet())));
        this.variables.put("lastDamage", new Variable(amount));

        this.mobSkills.onDamage(level, damageSource, amount);
        return super.hurtServer(level, damageSource, amount);
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return owner;
    }

    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        tracking.add(serverPlayer);
    }

    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        tracking.remove(serverPlayer);
        super.stopSeenByPlayer(serverPlayer);
    }

    public List<ServerPlayer> getTracking() {
        return tracking;
    }
}
