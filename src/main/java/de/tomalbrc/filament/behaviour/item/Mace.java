package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Mace-like behaviour
 */
public class Mace implements ItemBehaviour<Mace.Config> {
    private final Config config;

    public Mace(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Mace.Config getConfig() {
        return this.config;
    }

    @Override
    public Optional<Boolean> hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (MaceItem.canSmashAttack(livingEntity2)) {
            ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
            livingEntity2.setDeltaMovement(livingEntity2.getDeltaMovement().with(Direction.Axis.Y, 0.01F));
            if (livingEntity2 instanceof ServerPlayer serverPlayer) {
                serverPlayer.currentImpulseImpactPos = this.calculateImpactPosition(serverPlayer);
                serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }

            if (livingEntity.onGround()) {
                if (livingEntity2 instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setSpawnExtraParticlesOnFall(true);
                }

                SoundEvent soundEvent = livingEntity2.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), soundEvent, livingEntity2.getSoundSource(), 1.0F, 1.0F);
            } else {
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), SoundEvents.MACE_SMASH_AIR, livingEntity2.getSoundSource(), 1.0F, 1.0F);
            }

            MaceItem.knockback(serverLevel, livingEntity2, livingEntity);
        }

        return Optional.of(true);
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
        if (MaceItem.canSmashAttack(livingEntity2)) {
            livingEntity2.resetFallDistance();
        }

    }

    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        Entity damageSourceDirectEntity = damageSource.getDirectEntity();
        if (damageSourceDirectEntity instanceof LivingEntity livingEntity) {
            if (!MaceItem.canSmashAttack(livingEntity)) {
                return 0.0F;
            } else {
                float fallDistance = livingEntity.fallDistance;
                float fallDistanceMul;
                if (fallDistance <= 3.0F) {
                    fallDistanceMul = 4.0F * fallDistance;
                } else if (fallDistance <= 8.0F) {
                    fallDistanceMul = 12.0F + 2.0F * (fallDistance - 3.0F);
                } else {
                    fallDistanceMul = 22.0F + fallDistance - 8.0F;
                }

                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    return (fallDistanceMul + EnchantmentHelper.modifyFallBasedDamage(serverLevel, livingEntity.getWeaponItem(), entity, damageSource, 0.0F) * fallDistance) * config.damageMultiplier;
                } else {
                    return fallDistanceMul * config.damageMultiplier;
                }
            }
        } else {
            return 0.0F;
        }
    }

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity livingEntity) {
        return MaceItem.canSmashAttack(livingEntity) ? livingEntity.damageSources().mace(livingEntity) : null;
    }

    private Vec3 calculateImpactPosition(ServerPlayer serverPlayer) {
        return serverPlayer.isIgnoringFallDamageFromCurrentImpulse() && serverPlayer.currentImpulseImpactPos != null && serverPlayer.currentImpulseImpactPos.y <= serverPlayer.position().y ? serverPlayer.currentImpulseImpactPos : serverPlayer.position();
    }

    public static class Config {
        public float damageMultiplier = 1.0f;
    }
}