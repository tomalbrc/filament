package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.item.TridentEntity;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Trident behaviour
 */
public class Trident implements ItemBehaviour<Trident.Config>, ItemPredicateModelProvider {
    private final Config config;

    public Trident(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Trident.Config getConfig() {
        return this.config;
    }

    @Override
    public Optional<Integer> getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return Optional.of(72000);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (livingEntity instanceof Player player) {
            var item = itemStack.getItem();
            int j = this.getUseDuration(itemStack, livingEntity).orElseThrow() - i;
            if (j < 10) {
                return false;
            } else {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
                if (f > 0.0f && !player.isInWaterOrRain()) {
                    return false;
                } else if (itemStack.nextDamageWillBreak()) {
                    return false;
                } else {
                    Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
                    player.awardStat(Stats.ITEM_USED.get(item));
                    if (level instanceof ServerLevel serverLevel) {
                        itemStack.hurtWithoutBreaking(1, player);
                        if (f == 0.0f) {
                            TridentEntity tridentEntity = Projectile.spawnProjectileFromRotation(TridentEntity::new, serverLevel, itemStack, player, 0.0f, 2.5f, 1.0f);

                            if (player.hasInfiniteMaterials()) {
                                tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            } else {
                                player.getInventory().removeItem(itemStack);
                            }

                            level.playSound(null, tridentEntity, holder.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                            return true;
                        }
                    }

                    if (f > 0.0F) {
                        float g = player.getYRot();
                        float h = player.getXRot();
                        float k = -Mth.sin(g * Mth.DEG_TO_RAD) * Mth.cos(h * Mth.DEG_TO_RAD);
                        float l = -Mth.sin(h * Mth.DEG_TO_RAD);
                        float m = Mth.cos(g * Mth.DEG_TO_RAD) * Mth.cos(h * Mth.DEG_TO_RAD);
                        float n = Mth.sqrt(k * k + l * l + m * m);
                        k *= f / n;
                        l *= f / n;
                        m *= f / n;
                        player.push(k, l, m);
                        player.startAutoSpinAttack(20, 8.0F, itemStack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
                        }

                        level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult use(Item self, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void generate(Data<?> data) {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder ->
                ItemAssetGenerator.createTrident(
                        resourcePackBuilder, data.id(),
                        Objects.requireNonNull(data.itemResource()),
                        data.components().has(DataComponents.DYED_COLOR) || data.vanillaItem().components().has(DataComponents.DYED_COLOR)
                )
        );
    }

    @Override
    public List<String> requiredModels() {
        return List.of("throwing");
    }

    public static class Config {

    }
}