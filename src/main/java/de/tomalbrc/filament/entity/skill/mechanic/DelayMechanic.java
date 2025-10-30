package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;

public class DelayMechanic implements Mechanic {
    private final int ticks;

    public DelayMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public int execute(SkillContext context) {
        return ticks;
    }
}