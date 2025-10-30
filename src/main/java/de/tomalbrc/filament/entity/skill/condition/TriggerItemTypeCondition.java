package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

class TriggerItemTypeCondition implements Condition {
    private final ItemStack sample;

    TriggerItemTypeCondition(ItemStack s) {
        this.sample = s;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object it = ctx.vars().get("triggerItem");
        if (!(it instanceof ItemStack stack)) return false;
        return ItemStack.isSameItem(stack, sample);
    }
}
