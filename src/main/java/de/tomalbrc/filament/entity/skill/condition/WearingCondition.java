package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

class WearingCondition implements Condition {
    private final EquipmentSlot slot;
    private final ItemStack sample;

    WearingCondition(EquipmentSlot slot, ItemStack sample) {
        this.slot = slot;
        this.sample = sample;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ItemStack.isSameItem(target.getItemBySlot(slot), sample);
    }
}
