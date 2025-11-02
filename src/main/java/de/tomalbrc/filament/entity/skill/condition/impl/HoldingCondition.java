package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HoldingCondition implements Condition {
    //private final ItemStack sample;

    HoldingCondition(ItemStack s) {
        //this.sample = s;
    }

    public boolean test(SkillTree ctx, Target target) {
        //return ItemStack.isSameItem(target.getEntity().asLivingEntity().getItemBySlot(EquipmentSlot.MAINHAND), sample);
        return false;
    }
}
