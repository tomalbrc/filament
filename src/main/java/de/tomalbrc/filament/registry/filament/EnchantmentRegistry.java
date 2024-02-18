package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.filament.enchantment.*;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings("unused")
public class EnchantmentRegistry {
    public static Enchantment BLACKENED_EDGE = register(new ResourceLocation(Constants.MOD_ID, "blackened_edge"), new BlackenedEdgeEnchantment());
    public static Enchantment LETHARGY = register(new ResourceLocation(Constants.MOD_ID, "lethargy"), new LethargyEnchantment());
    public static Enchantment LUMINOUS_BLADE = register(new ResourceLocation(Constants.MOD_ID, "luminous_blade"), new LuminousBladeEnchantment());
    public static Enchantment WINTERS_GRASP = register(new ResourceLocation(Constants.MOD_ID, "winters_grasp"), new WintersGraspEnchantment());
    public static Enchantment MAGNETIZED = register(new ResourceLocation(Constants.MOD_ID, "magnetized"), new MagnetizedEnchantment());
    public static Enchantment INFERNAL_TOUCH = register(new ResourceLocation(Constants.MOD_ID, "infernal_touch"), new InfernalTouchEnchantment());

    public static void register() {
    }

    private static Enchantment register(ResourceLocation id, Enchantment enchantment) {
        Registry.register(BuiltInRegistries.ENCHANTMENT, id, enchantment);
        return enchantment;
    }
}
