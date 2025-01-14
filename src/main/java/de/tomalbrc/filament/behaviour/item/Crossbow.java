package de.tomalbrc.filament.behaviour.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Crossbow behaviour
 */
public class Crossbow implements ItemBehaviour<Crossbow.Config>, ItemPredicateModelProvider {
    private final CrossbowItem.ChargingSounds sounds;

    private final Config config;
    private boolean startSoundPlayed;
    private boolean midLoadSoundPlayed;

    public Crossbow(Config config) {
        this.config = config;
        this.sounds = new CrossbowItem.ChargingSounds(
                Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvent.createVariableRangeEvent(config.loadingStartSound))),
                Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvent.createVariableRangeEvent(config.loadingMiddleSound))),
                Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvent.createVariableRangeEvent(config.loadingEndSound)))
        );
    }

    @Override
    @NotNull
    public Crossbow.Config getConfig() {
        return this.config;
    }

    @Override
    public void modifyPolymerItemStack(ItemStack original, ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        // polymer removes that component, so we have to add it back again.
        itemStack.set(DataComponents.CHARGED_PROJECTILES, original.get(DataComponents.CHARGED_PROJECTILES));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            this.performShooting(level, player, interactionHand, itemStack, getShootingPower(chargedProjectiles) * config.powerMultiplier, 1.0F, null);
            return InteractionResultHolder.consume(itemStack);
        } else if (!player.getProjectile(itemStack).isEmpty()) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    private static float getShootingPower(ChargedProjectiles chargedProjectiles) {
        return chargedProjectiles.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        int j = this.getUseDuration(itemStack, livingEntity).orElseThrow() - i;
        float f = CrossbowItem.getPowerForTime(j, itemStack, livingEntity);
        if (f >= 1.f && !CrossbowItem.isCharged(itemStack) && CrossbowItem.tryLoadProjectiles(livingEntity, itemStack)) {
            CrossbowItem.ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
            chargingSounds.end().ifPresent((holder) -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), holder.value(), livingEntity.getSoundSource(), 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F));
        }
    }

    protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float f, float g, float h, @Nullable LivingEntity livingEntity2) {
        Vector3f vector3f;
        if (livingEntity2 != null) {
            double d = livingEntity2.getX() - livingEntity.getX();
            double e = livingEntity2.getZ() - livingEntity.getZ();
            double j = Math.sqrt(d * d + e * e);
            double k = livingEntity2.getY(1.0/3.0) - projectile.getY() + j * 0.2;
            vector3f = getProjectileShotVector(livingEntity, new Vec3(d, k, e), h);
        } else {
            Vec3 vec3 = livingEntity.getUpVector(1.0F);
            Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((h * 0.017453292F), vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = livingEntity.getViewVector(1.0F);
            vector3f = vec32.toVector3f().rotate(quaternionf);
        }

        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), f, g);
        float l = getShotPitch(livingEntity.getRandom(), i);
        livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvent.createVariableRangeEvent(this.config.shootSound), livingEntity.getSoundSource(), 1.0F, l);
    }

    private static Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
        Vector3f vector3f = vec3.toVector3f().normalize();
        Vector3f vector3f2 = (new Vector3f(vector3f)).cross(new Vector3f(0.0F, 1.0F, 0.0F));
        if (vector3f2.lengthSquared() <= 1.0E-7) {
            Vec3 vec32 = livingEntity.getUpVector(1.0F);
            vector3f2 = (new Vector3f(vector3f)).cross(vec32.toVector3f());
        }

        Vector3f vector3f3 = (new Vector3f(vector3f)).rotateAxis(Mth.HALF_PI, vector3f2.x, vector3f2.y, vector3f2.z);
        return (new Vector3f(vector3f)).rotateAxis(f * 0.017453292F, vector3f3.x, vector3f3.y, vector3f3.z);
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

    protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        if (itemStack2.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, itemStack2, livingEntity, livingEntity.getX(), livingEntity.getEyeY() - 0.15000000596046448, livingEntity.getZ(), true);
        } else {
            Projectile projectile = createArrow(level, livingEntity, itemStack, itemStack2, bl);
            if (projectile instanceof AbstractArrow abstractArrow) {
                abstractArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
            }

            return projectile;
        }
    }

    protected int getDurabilityUse(ItemStack itemStack) {
        return itemStack.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    public void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float g, @Nullable LivingEntity livingEntity2) {
        if (level instanceof ServerLevel serverLevel) {
            ChargedProjectiles chargedProjectiles = itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
                this.shoot(serverLevel, livingEntity, interactionHand, itemStack, chargedProjectiles.getItems(), f, g, livingEntity instanceof Player, livingEntity2);
                if (livingEntity instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
                    serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                }
            }
        }
    }

    protected void shoot(ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, List<ItemStack> list, float f, float g, boolean bl, @Nullable LivingEntity livingEntity2) {
        float h = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, livingEntity, 0.0F);
        float i = list.size() == 1 ? 0.0F : 2.0F * h / (float)(list.size() - 1);
        float j = (float)((list.size() - 1) % 2) * i / 2.0F;
        float k = 1.0F;

        for(int l = 0; l < list.size(); ++l) {
            ItemStack itemStack2 = list.get(l);
            if (!itemStack2.isEmpty()) {
                float m = j + k * (float)((l + 1) / 2) * i;
                k = -k;
                Projectile projectile = this.createProjectile(serverLevel, livingEntity, itemStack, itemStack2, bl);
                this.shootProjectile(livingEntity, projectile, l, f, g, m, livingEntity2);
                serverLevel.addFreshEntity(projectile);
                itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, LivingEntity.getSlotForHand(interactionHand));
                if (itemStack.isEmpty()) {
                    break;
                }
            }
        }

    }

    private static float getShotPitch(RandomSource randomSource, int i) {
        return i == 0 ? 1.0F : getRandomShotPitch((i & 1) == 1, randomSource);
    }

    private static float getRandomShotPitch(boolean bl, RandomSource randomSource) {
        float f = bl ? 0.63F : 0.43F;
        return 1.0F / (randomSource.nextFloat() * 0.5F + 1.8F) + f;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide) {
            CrossbowItem.ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
            float f = (float)(itemStack.getUseDuration(livingEntity) - i) / (float)getChargeDuration(itemStack, livingEntity);
            if (f < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                chargingSounds.start().ifPresent((holder) -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), holder.value(), SoundSource.PLAYERS, 0.5F, 1.0F));
            }

            if (f >= 0.5F && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                chargingSounds.mid().ifPresent((holder) -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), holder.value(), SoundSource.PLAYERS, 0.5F, 1.0F));
            }
        }

    }

    @Override
    public Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.of(getChargeDuration(itemStack, livingEntity) + 3);
    }

    public static int getChargeDuration(ItemStack itemStack, LivingEntity livingEntity) {
        float f = EnchantmentHelper.modifyCrossbowChargingTime(itemStack, livingEntity, 1.25F);
        return Mth.floor(f * 20.0F);
    }

    CrossbowItem.ChargingSounds getChargingSounds(ItemStack itemStack) {
        return EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS).orElse(this.sounds);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            ItemStack itemStack2 = chargedProjectiles.getItems().getFirst();
            list.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack2.getDisplayName()));
            if (tooltipFlag.isAdvanced() && itemStack2.is(Items.FIREWORK_ROCKET)) {
                List<Component> list2 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemStack2, tooltipContext, list2, tooltipFlag);
                if (!list2.isEmpty()) {
                    list2.replaceAll(component -> Component.literal("  ").append(component).withStyle(ChatFormatting.GRAY));
                    list.addAll(list2);
                }
            }
        }
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        return itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.CROSSBOW);
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

    @Override
    public void generate(ResourceLocation id, ItemResource itemResource) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder ->
            ItemAssetGenerator.createCrossbow(
                resourcePackBuilder, id,
                itemResource
            )
        );
    }


    public static class Config {
        /**
         * Power multiplier for the projectile
         */
        public float powerMultiplier = 1.f;

        public List<ResourceLocation> supportedProjectiles = ImmutableList.of(ResourceLocation.withDefaultNamespace("arrow"), ResourceLocation.withDefaultNamespace("spectral_arrow"), ResourceLocation.withDefaultNamespace("firework_rocket"));
        public List<ResourceLocation> supportedHeldProjectiles = ImmutableList.of(ResourceLocation.withDefaultNamespace("arrow"), ResourceLocation.withDefaultNamespace("spectral_arrow"), ResourceLocation.withDefaultNamespace("firework_rocket"));

        public ResourceLocation shootSound = SoundEvents.CROSSBOW_SHOOT.location();

        public ResourceLocation loadingStartSound = SoundEvents.CROSSBOW_LOADING_START.value().location();
        public ResourceLocation loadingMiddleSound = SoundEvents.CROSSBOW_LOADING_MIDDLE.value().location();
        public ResourceLocation loadingEndSound = SoundEvents.CROSSBOW_LOADING_END.value().location();
    }
}