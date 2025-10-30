package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

class BlockingCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        if (!(target instanceof ServerPlayer p)) return false;
        ItemStack stack = p.getItemInHand(p.getUsedItemHand());
        return stack.is(ConventionalItemTags.SHIELD_TOOLS) && p.isUsingItem();
    }
}