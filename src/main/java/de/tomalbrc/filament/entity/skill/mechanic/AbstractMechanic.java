package de.tomalbrc.filament.entity.skill.mechanic;

public abstract class AbstractMechanic implements Mechanic {
    final int delay;

    public AbstractMechanic(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }
}
