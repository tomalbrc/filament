package de.tomalbrc.filament.enchantment;

import eu.pb4.polymer.core.api.other.PolymerEnchantment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import de.tomalbrc.filament.registry.filament.EnchantmentRegistry;

import java.util.Optional;

public class BlackenedEdgeEnchantment extends Enchantment implements PolymerEnchantment {
    public BlackenedEdgeEnchantment() {
        super(new EnchantmentDefinition(ItemTags.WEAPON_ENCHANTABLE, Optional.empty(), 1, 2, Enchantment.dynamicCost(0, 26), Enchantment.dynamicCost(10, 26), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FIRE_ASPECT && enchantment != EnchantmentRegistry.WINTERS_GRASP;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 6, level - 1));
        }
        super.doPostAttack(user, target, level);
    }

    @Override
    public Enchantment getPolymerReplacement(ServerPlayer player) {
        return null;
    }
}