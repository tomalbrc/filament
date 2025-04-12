package de.tomalbrc.filament.entity;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.data.EntityData;
import de.tomalbrc.filament.registry.EntityRegistry;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

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

        //this.setPathfindingMalus(PathType.BLOCKED);

        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
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
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (data.properties().category == MobCategory.MONSTER) return -levelReader.getPathfindingCostFromLightLevels(blockPos);
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return data.properties().shouldDespawnInPeaceful;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
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
    public boolean canPickUpLoot() {
        return this.data.properties().canPickupLoot;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
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
    protected SoundEvent getHurtSound(DamageSource damageSource) {
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
    public boolean isFood(ItemStack itemStack) {
        return false;
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

    //@Override
    //public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        //BuiltInRegistries.ENTITY_TYPE.getValue(this.data.entityType());
    //}
}
