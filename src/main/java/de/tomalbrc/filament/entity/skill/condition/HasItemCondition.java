package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class HasItemCondition implements Condition {
    private final ItemStack sample;
    private final int min;

    HasItemCondition(ItemStack s, int min) {
        this.sample = s;
        this.min = min;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof Player p)) return false;
        int cnt = 0;
        for (ItemStack it : p.getInventory()) if (ItemStack.isSameItem(it, sample)) cnt++;
        return cnt >= min;
    }
}
