package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class BlockingCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        if (!(target.getEntity() instanceof ServerPlayer p)) return false;
        ItemStack stack = p.getItemInHand(p.getUsedItemHand());
        return stack.is(ConventionalItemTags.SHIELD_TOOLS) && p.isUsingItem();
    }
}