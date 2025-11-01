package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.EquipmentSlot;

class HasOffhandCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return !target.getEntity().asLivingEntity().getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }
}
