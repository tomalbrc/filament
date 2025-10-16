package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.registry.EntityRegistry;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Item behaviour for projectile shooting
 */
public class Shoot implements ItemBehaviour<Shoot.Config> {
    private final Config config;

    public Shoot(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Shoot.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        ItemBehaviour.super.init(item, behaviourHolder);
        if (config.dispenserSupport) DispenserBlock.registerBehavior(item, new Snowball.SnowballDispenseBehavior(item, ProjectileItem.DispenseConfig.DEFAULT));
    }

    @Override
    public InteractionResult use(Item item, Level level, Player user, InteractionHand hand) {
        user.getCooldowns().addCooldown(user.getItemInHand(hand), config.cooldown);
        ItemStack itemStack = user.getItemInHand(hand);

        if (!level.isClientSide()) {
            BaseProjectileEntity projectile = EntityRegistry.BASE_PROJECTILE.create(level, EntitySpawnReason.TRIGGERED);
            if (projectile != null) {
                projectile.config = config;

                projectile.setPos(user.position().add(0, user.getEyeHeight(), 0));
                Util.damageAndBreak(1, itemStack, user, Player.getSlotForHand(hand));

                float pitch = user.getXRot();
                float yaw = user.getYRot();
                double speed = this.config.speed; // Adjust the speed as needed

                Vector3f deltaMovement = new Vector3f(
                        -(float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
                        -(float) Math.sin(Math.toRadians(pitch)),
                        (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
                ).mul((float) speed);

                projectile.setYRot(user.getYRot());
                projectile.setXRot(user.getXRot());
                projectile.setDeltaMovement(deltaMovement.x, deltaMovement.y, deltaMovement.z);

                ItemStack projItem = this.config.projectile != null ? BuiltInRegistries.ITEM.getValue(this.config.projectile).getDefaultInstance() : itemStack.copyWithCount(1);
                ItemStack pickupItem = this.config.pickupItem != null ? BuiltInRegistries.ITEM.getValue(this.config.pickupItem).getDefaultInstance() : projItem;
                projectile.setOwner(user);
                projectile.setPickupStack(pickupItem);
                projectile.setBase(itemStack.copy());
                projectile.setProjectileStack(projItem);
                projectile.setBaseDamage(this.config.baseDamage);

                if (user.isCreative()) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            level.addFreshEntity(projectile);
            level.playSound(null, projectile, this.config.sound != null ? BuiltInRegistries.SOUND_EVENT.getValue(this.config.sound) : SoundEvents.TRIDENT_THROW.value(), SoundSource.NEUTRAL, config.volume, config.pitch);
            if (!user.isCreative()) {
                if (this.config.consumes) itemStack.shrink(1);
                else itemStack.hurtAndBreak(1, user, hand);
            }
        }

        user.awardStat(Stats.ITEM_USED.get(item));

        return InteractionResult.CONSUME;
    }

    public static class Config {
        /**
         * Indicates whether the shooting action consumes the item
         */
        public boolean consumes;

        /**
         * Indicates whether the item gets damaged when using
         */
        public boolean damages;

        /**
         * The base damage of the projectile.
         */
        public double baseDamage = 2.0;

        /**
         * The speed at which the projectile is fired.
         */
        public double speed = 1.0;

        /**
         * Cooldown ticks
         */
        public int cooldown = 8;

        /**
         * The identifier for the projectile item
         */
        public ResourceLocation projectile;

        public ResourceLocation pickupItem;

        public boolean dispenserSupport = false;
        public boolean canPickUp = false;
        public boolean dropAsItem = true;

        /**
         * Sound effect to play when shooting
         */
        public ResourceLocation sound = SoundEvents.TRIDENT_THROW.value().location();
        public float volume = 1.f;
        public float pitch = 1.f;

        public ResourceLocation hitSound = SoundEvents.TRIDENT_HIT.location();
        public float hitVolume = 1.f;
        public float hitPitch = 1.f;

        public ResourceLocation hitGroundSound = SoundEvents.TRIDENT_HIT_GROUND.location();

        /**
         * Display context of the item display
         */
        public ItemDisplayContext display = ItemDisplayContext.NONE;

        /**
         * Rotation
         */
        public Quaternionf rotation = new Quaternionf().rotationY(Mth.DEG_TO_RAD*90);

        /**
         * Translation
         */
        public Vector3f translation = new Vector3f();

        /**
         * Scale
         */
        public Vector3f scale = new Vector3f(0.6f);
    }
}
