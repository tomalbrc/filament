package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class TriggerBlockTypeCondition implements Condition {
    private final Block block;

    TriggerBlockTypeCondition(Block b) {
        this.block = b;
    }

    public boolean test(SkillContext ctx, Target target) {
        Object t = ctx.vars().get("triggerBlock");
        if (!(t instanceof BlockState bs)) return false;
        return bs.getBlock() == block;
    }
}
