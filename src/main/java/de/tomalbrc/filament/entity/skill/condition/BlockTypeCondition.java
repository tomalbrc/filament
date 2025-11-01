package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class BlockTypeCondition implements Condition {
    private final Set<Block> blocks;

    BlockTypeCondition(Collection<Block> blocks) {
        this.blocks = new HashSet<>(blocks);
    }

    public boolean test(SkillContext ctx, Target target) {
        return blocks.contains(ctx.level().getBlockState(target.getBlockPos()).getBlock());
    }
}
