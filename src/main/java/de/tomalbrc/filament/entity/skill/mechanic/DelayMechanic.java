package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.resources.ResourceLocation;

public class DelayMechanic implements Mechanic {
    private final int ticks;

    public DelayMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public int execute(SkillContext context) {
        return ticks;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.DELAY;
    }
}