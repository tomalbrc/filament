package de.tomalbrc.filament.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;
/*
public class LuminousBladeEnchantment extends Enchantment implements PolymerEnchantment {
    public LuminousBladeEnchantment() {
        super(new EnchantmentDefinition(ItemTags.WEAPON_ENCHANTABLE, Optional.empty(), 5, 5, Enchantment.dynamicCost(4, 12), Enchantment.dynamicCost(4, 12), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 5 * level, 0));
        }

        super.doPostAttack(user, target, level);
    }

    @Override
    public Enchantment getPolymerReplacement(ServerPlayer player) {
        return null;
    }
}
 */