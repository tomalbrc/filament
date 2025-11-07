package de.tomalbrc.filament.entity.skill.mechanic;

public abstract class AbstractMechanic implements Mechanic {
    final int delay;

    public AbstractMechanic(int delay) {
        this.delay = delay;
    }

    public AbstractMechanic() {
        this(0);
    }

    // TODO: cause delay before running in new skilltree maybe
    public int getDelay() {
        return delay;
    }
}
