package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

class HasOffhandCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        return !target.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }
}
