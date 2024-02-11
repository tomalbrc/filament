package de.tomalbrc.filament.data.misc;

import net.minecraft.world.entity.EquipmentSlot;

// BIG TODO, for armor etc

@SuppressWarnings("unused")
public record AttributeModifier(
        String attribute,
        float value,
        EquipmentSlot slot
) {}
