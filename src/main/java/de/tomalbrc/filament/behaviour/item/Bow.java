package de.tomalbrc.filament.behaviour.item;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Fuel behaviour
 */
public class Bow implements ItemBehaviour<Bow.Config> {
    private final Config config;

    public Bow(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Bow.Config getConfig() {
        return this.config;
    }

    @Override
    public Optional<Integer> getEnchantmentValue() {
        return Optional.of(1);
    }

    protected void shoot(ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, List<ItemStack> list, float f, boolean fullPower) {
        float spread = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, livingEntity, 0.0f);
        float i = list.size() == 1 ? 0.0f : 2.0f * spread / (float)(list.size() - 1);
        float j = (float)((list.size() - 1) % 2) * i / 2.0f;
        float k = 1.0f;
        for (int l = 0; l < list.size(); ++l) {
            ItemStack itemStack2 = list.get(l);
            if (itemStack2.isEmpty()) continue;
            float m = j + k * (float)((l + 1) / 2) * i;
            k = -k;
            Projectile projectile = this.createProjectile(serverLevel, livingEntity, itemStack, itemStack2, fullPower);
            this.shootProjectile(livingEntity, projectile, f, (float) 1.0, m);
            serverLevel.addFreshEntity(projectile);
            itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, LivingEntity.getSlotForHand(interactionHand));
            if (itemStack.isEmpty()) break;
        }
    }

    protected int getDurabilityUse(ItemStack itemStack) {
        return itemStack.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        if (itemStack2.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, itemStack2, livingEntity, livingEntity.getX(), livingEntity.getEyeY() - 0.15000000596046448, livingEntity.getZ(), true);
        } else {
            return createArrow(level, livingEntity, itemStack, itemStack2, bl);
        }
    }

    protected Projectile createArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean crit) {
        Item item = itemStack2.getItem();
        ArrowItem arrowItem = item instanceof ArrowItem ? (ArrowItem)item : (ArrowItem) Items.ARROW;
        AbstractArrow abstractArrow = arrowItem.createArrow(level, itemStack2, livingEntity, itemStack);
        if (crit) {
            abstractArrow.setCritArrow(true);
        }
        return abstractArrow;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        ItemStack itemStack2 = player.getProjectile(itemStack);
        if (itemStack2.isEmpty()) {
            return;
        }
        int j = this.getUseDuration(itemStack, livingEntity).orElseThrow() - useDuration;
        float currentPower = BowItem.getPowerForTime(j);
        if (currentPower < 0.1) {
            return;
        }

        List<ItemStack> list = BowItem.draw(itemStack, itemStack2, player);
        if (level instanceof ServerLevel serverLevel && !list.isEmpty()) {
            this.shoot(serverLevel, player, player.getUsedItemHand(), itemStack, list, currentPower * config.powerMultiplier, currentPower == 1.0f);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), config.shootSound, SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + currentPower * 0.5f);
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
    }

    public void shootProjectile(LivingEntity livingEntity, Projectile projectile, float f, float g, float h) {
        projectile.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot() + h, 0.0f, f, g);
    }

    @Override
    public Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.of(72000);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean hasProjectile = !player.getProjectile(itemStack).isEmpty();
        if (player.hasInfiniteMaterials() || hasProjectile) {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    public Predicate<ItemStack> supportedProjectiles() {
        return itemStack -> {
            for (var itemId : config.supportedProjectiles) {
                if (itemStack.is(BuiltInRegistries.ITEM.get(itemId)))
                    return true;
            }
            return false;
        };
    }

    public Predicate<ItemStack> supportedHeldProjectiles() {
        return itemStack -> {
            for (var itemId : config.supportedHeldProjectiles) {
                if (itemStack.is(BuiltInRegistries.ITEM.get(itemId)))
                    return true;
            }
            return false;
        };
    }

    public static class Config {
        /**
         * Power multiplier for the projectile
         */
        public float powerMultiplier = 3.f;

        public List<ResourceLocation> supportedProjectiles = ImmutableList.of(ResourceLocation.withDefaultNamespace("arrow"), ResourceLocation.withDefaultNamespace("spectral_arrow"));
        public List<ResourceLocation> supportedHeldProjectiles = ImmutableList.of(ResourceLocation.withDefaultNamespace("arrow"), ResourceLocation.withDefaultNamespace("spectral_arrow"), ResourceLocation.withDefaultNamespace("firework_rocket"));

        public SoundEvent shootSound = SoundEvents.ARROW_SHOOT;
    }
}