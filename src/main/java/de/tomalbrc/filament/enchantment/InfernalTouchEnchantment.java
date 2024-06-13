package de.tomalbrc.filament.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

/*
public class InfernalTouchEnchantment extends Enchantment implements PolymerEnchantment {
    public InfernalTouchEnchantment() {
        super(new EnchantmentDefinition(ItemTags.MINING_LOOT_ENCHANTABLE, Optional.empty(), 2, 1, Enchantment.dynamicCost(0, 40), Enchantment.dynamicCost(20, 40), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.SILK_TOUCH;
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return super.canEnchant(itemStack) && itemStack.getItem() instanceof PickaxeItem;
    }
}
*/