package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import net.minecraft.resources.ResourceLocation;

public class DelayMechanic implements Mechanic {
    private final int ticks;

    public DelayMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public int execute(SkillTree tree) {
        return ticks;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.DELAY;
    }
}