package de.tomalbrc.filament.enchantment;

import eu.pb4.polymer.core.api.other.PolymerEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class InfernalTouchEnchantment extends Enchantment implements PolymerEnchantment {
    public InfernalTouchEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return level*50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
