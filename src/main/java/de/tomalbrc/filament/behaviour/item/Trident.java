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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
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
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (livingEntity instanceof Player player) {
            int j = this.getUseDuration(itemStack, livingEntity).orElseThrow() - i;
            if (j >= 10) {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
                if (!(f > 0.0F) || player.isInWaterOrRain()) {
                    if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
                        Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
                        if (!level.isClientSide) {
                            itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                            if (f == 0.0F) {
                                ThrownTrident thrownTrident = new ThrownTrident(level, player, itemStack);
                                thrownTrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                                if (player.hasInfiniteMaterials()) {
                                    thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                }

                                level.addFreshEntity(thrownTrident);
                                level.playSound(null, thrownTrident, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                if (!player.hasInfiniteMaterials()) {
                                    player.getInventory().removeItem(itemStack);
                                }
                            }
                        }

                        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                        if (f > 0.0F) {
                            float g = player.getYRot();
                            float h = player.getXRot();
                            float k = -Mth.sin(g * ((float)Math.PI / 180F)) * Mth.cos(h * ((float)Math.PI / 180F));
                            float l = -Mth.sin(h * ((float)Math.PI / 180F));
                            float m = Mth.cos(g * ((float)Math.PI / 180F)) * Mth.cos(h * ((float)Math.PI / 180F));
                            float n = Mth.sqrt(k * k + l * l + m * m);
                            k *= f / n;
                            l *= f / n;
                            m *= f / n;
                            player.push((double)k, (double)l, (double)m);
                            player.startAutoSpinAttack(20, 8.0F, itemStack);
                            if (player.onGround()) {
                                float o = 1.1999999F;
                                player.move(MoverType.SELF, new Vec3((double)0.0F, (double)1.1999999F, (double)0.0F));
                            }

                            level.playSound((Player)null, player, (SoundEvent)holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemStack);
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
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