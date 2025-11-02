package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class WearingCondition implements Condition {
    private final EquipmentSlot slot;
    //private final ItemStack sample;

    WearingCondition(EquipmentSlot slot, ItemStack sample) {
        this.slot = slot;
        //this.sample = sample;
    }

    public boolean test(SkillTree ctx, Target target) {
        //return ItemStack.isSameItem(target.getEntity().asLivingEntity().getItemBySlot(slot), sample);
        return false;
    }
}
