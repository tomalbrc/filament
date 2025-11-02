package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HasItemCondition implements Condition {
    //private final ItemStack sample;
    private final int min;

    HasItemCondition(ItemStack s, int min) {
        //this.sample = s;
        this.min = min;
    }

    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof Player p)) return false;
        int cnt = 0;
        //for (ItemStack it : p.getInventory()) if (ItemStack.isSameItem(it, sample)) cnt++;
        return cnt >= min;
    }
}
