package de.tomalbrc.filament.enchantment;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.util.Optional;

/*
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);
 */
/*
public class MagnetizedEnchantment extends Enchantment implements PolymerEnchantment {
    public MagnetizedEnchantment() {
        super(new EnchantmentDefinition(ItemTags.MINING_LOOT_ENCHANTABLE, Optional.empty(), 5, 1, Enchantment.dynamicCost(5, 19), Enchantment.dynamicCost(56, 19), 3, FeatureFlagSet.of(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment);
    }
}
*/