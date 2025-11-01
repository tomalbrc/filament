package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

class WearingCondition implements Condition {
    private final EquipmentSlot slot;
    private final ItemStack sample;

    WearingCondition(EquipmentSlot slot, ItemStack sample) {
        this.slot = slot;
        this.sample = sample;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ItemStack.isSameItem(target.getEntity().asLivingEntity().getItemBySlot(slot), sample);
    }
}
