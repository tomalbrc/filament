package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class HasFreeInventorySlotCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof Player p)) return false;
        for (ItemStack it : p.getInventory()) if (it.isEmpty()) return true;
        return false;
    }
}
