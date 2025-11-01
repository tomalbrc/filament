package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class HasFreeInventorySlotCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        for (ItemStack it : p.getInventory()) if (it.isEmpty()) return true;
        return false;
    }
}
