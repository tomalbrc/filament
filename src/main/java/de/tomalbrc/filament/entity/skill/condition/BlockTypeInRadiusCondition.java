package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

class BlockTypeInRadiusCondition implements Condition {
    private final Block block;
    private final int minCount;
    private final double radius;

    BlockTypeInRadiusCondition(Block block, int minCount, double radius) {
        this.block = block;
        this.minCount = minCount;
        this.radius = radius;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        int cnt = 0;
        for (int x = (int) Math.floor(target.getX() - radius); x <= (int) Math.ceil(target.getX() + radius); x++)
            for (int y = (int) Math.floor(target.getY() - radius); y <= (int) Math.ceil(target.getY() + radius); y++)
                for (int z = (int) Math.floor(target.getZ() - radius); z <= (int) Math.ceil(target.getZ() + radius); z++)
                    if (ctx.level().getBlockState(new BlockPos(x, y, z)).getBlock() == block) ++cnt;
        return cnt >= minCount;
    }
}
