package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HasFreeInventorySlotCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        for (ItemStack it : p.getInventory()) if (it.isEmpty()) return true;
        return false;
    }
}
