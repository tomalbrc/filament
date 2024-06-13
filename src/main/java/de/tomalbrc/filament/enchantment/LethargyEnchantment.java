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
public class LethargyEnchantment extends Enchantment implements PolymerEnchantment {
    public LethargyEnchantment() {
        super(new EnchantmentDefinition(ItemTags.WEAPON_ENCHANTABLE, Optional.empty(), 10, 4, Enchantment.dynamicCost(1, 10), Enchantment.dynamicCost(51, 10), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3 * level, level - 1));
        }

        super.doPostAttack(user, target, level);
    }

    @Override
    public Enchantment getPolymerReplacement(ServerPlayer player) {
        return null;
    }
}
 */