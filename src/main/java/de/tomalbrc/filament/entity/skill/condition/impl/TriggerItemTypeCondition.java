package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.item.ItemStack;

public class TriggerItemTypeCondition implements Condition {
    //private final ItemStack sample;

    TriggerItemTypeCondition(ItemStack s) {
        //this.sample = s;
    }

    public boolean test(SkillTree ctx, Target target) {
        //Object it = ctx.vars().get("triggerItem");
        //if (!(it instanceof ItemStack stack)) return false;
        //return ItemStack.isSameItem(stack, sample);
        return false;
    }
}
