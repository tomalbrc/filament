package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import net.minecraft.resources.ResourceLocation;

public class DelayMechanic implements Mechanic {
    protected final int ticks;
    transient final ExecutionResult result;

    public DelayMechanic(int ticks) {
        this.ticks = ticks;
        this.result = ExecutionResult.delayed(ticks);
    }

    @Override
    public ExecutionResult execute(SkillTree tree) {
        return result;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.DELAY;
    }
}