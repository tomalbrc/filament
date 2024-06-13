package de.tomalbrc.filament.enchantment;

import de.tomalbrc.filament.registry.filament.EnchantmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

/*
public class WintersGraspEnchantment extends Enchantment implements PolymerEnchantment {
    public WintersGraspEnchantment() {
        super(new EnchantmentDefinition(ItemTags.WEAPON_ENCHANTABLE, Optional.empty(), 2, 3, Enchantment.dynamicCost(8, 20), Enchantment.dynamicCost(58, 20), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FIRE_ASPECT && enchantment != EnchantmentRegistry.BLACKENED_EDGE && enchantment != EnchantmentRegistry.LETHARGY;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2 * level, level - 1));
            livingEntity.setTicksFrozen(220 * level);
        }

        super.doPostAttack(user, target, level);
    }

    @Override
    public Enchantment getPolymerReplacement(ServerPlayer player) {
        return null;
    }
}
*/
