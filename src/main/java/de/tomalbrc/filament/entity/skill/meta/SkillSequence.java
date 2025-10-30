package de.tomalbrc.filament.entity.skill.meta;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;

import java.util.List;

public class SkillSequence {
    private final List<Mechanic> actions;
    private int currentIndex = 0;
    private int delayRemaining = 0;

    public SkillSequence(List<Mechanic> actions) {
        this.actions = actions;
    }

    public void tick(SkillContext context) {
        if (delayRemaining > 0) {
            delayRemaining--;
            return;
        }

        while (currentIndex < actions.size()) {
            Mechanic action = actions.get(currentIndex++);
            int delay = action.execute(context);
            if (delay > 0) {
                delayRemaining = delay;
                break;
            }
        }
    }

    public boolean isFinished() {
        return currentIndex >= actions.size() && delayRemaining == 0;
    }
}
