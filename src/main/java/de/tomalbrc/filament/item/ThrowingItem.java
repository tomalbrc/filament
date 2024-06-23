package de.tomalbrc.filament.item;

import de.tomalbrc.filament.data.behaviours.item.Shoot;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import de.tomalbrc.filament.registry.filament.EntityRegistry;

public class ThrowingItem extends SimpleItem implements PolymerItem {
    private final Shoot shootBehaviour;

    public ThrowingItem(Item.Properties properties, ItemData itemData) {
        super(properties, itemData);
        assert itemData.behaviour() != null;
        this.shootBehaviour = itemData.behaviour().shoot;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        var res = super.use(level, user, hand);

        user.getCooldowns().addCooldown(this, 8);
        ItemStack itemStack = user.getItemInHand(hand);

        if (!level.isClientSide) {
            BaseProjectileEntity projectile = EntityRegistry.BASE_PROJECTILE.create(level);
            if (projectile != null) {
                projectile.setPos(user.position().add(0, user.getEyeHeight(), 0));
                Util.damageAndBreak(1, itemStack, user, Player.getSlotForHand(hand));

                float pitch = user.getXRot();
                float yaw = user.getYRot();
                double speed = this.shootBehaviour.speed; // Adjust the speed as needed

                Vector3f deltaMovement = new Vector3f(
                        -(float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
                        -(float) Math.sin(Math.toRadians(pitch)),
                        (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
                ).mul((float) speed);

                projectile.setYRot(user.getYRot());
                projectile.setXRot(user.getXRot());
                projectile.setDeltaMovement(deltaMovement.x, deltaMovement.y, deltaMovement.z);

                var projItem = this.shootBehaviour.projectile != null ? BuiltInRegistries.ITEM.get(this.shootBehaviour.projectile).getDefaultInstance() : itemStack.copyWithCount(1);
                projectile.setProjectileStack(projItem);
                projectile.setPickupStack(projItem);
                projectile.setOwner(user);
                projectile.setBaseDamage(this.shootBehaviour.baseDamage);

                if (user.isCreative()) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            level.addFreshEntity(projectile);
            level.playSound(null, projectile, this.shootBehaviour.sound != null ? BuiltInRegistries.SOUND_EVENT.get(this.shootBehaviour.sound) : SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!user.isCreative() && this.shootBehaviour.consumes) {
                itemStack.shrink(1);
            }
        }

        if (res.getResult() == InteractionResult.CONSUME) user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.consume(itemStack);
    }
}
