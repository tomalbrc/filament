package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

class ItemTypeCondition implements Condition {
    private final ItemStack item;

    ItemTypeCondition(ItemStack i) {
        this.item = i;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().get("triggerItem") instanceof ItemStack itemStack && ItemStack.isSameItem(item, itemStack);
    }
}