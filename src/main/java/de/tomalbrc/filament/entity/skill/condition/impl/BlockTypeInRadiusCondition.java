package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class BlockTypeInRadiusCondition implements Condition {
    private final Block block;
    private final int minCount;
    private final double radius;

    BlockTypeInRadiusCondition(Block block, int minCount, double radius) {
        this.block = block;
        this.minCount = minCount;
        this.radius = radius;
    }

    public boolean test(SkillTree ctx, Target target) {
        int cnt = 0;
        for (int x = (int) Math.floor(target.getBlockPos().getX() - radius); x <= (int) Math.ceil(target.getBlockPos().getX() + radius); x++)
            for (int y = (int) Math.floor(target.getBlockPos().getY() - radius); y <= (int) Math.ceil(target.getBlockPos().getY() + radius); y++)
                for (int z = (int) Math.floor(target.getBlockPos().getZ() - radius); z <= (int) Math.ceil(target.getBlockPos().getZ() + radius); z++)
                    if (ctx.level().getBlockState(new BlockPos(x, y, z)).getBlock() == block) ++cnt;
        return cnt >= minCount;
    }
}
