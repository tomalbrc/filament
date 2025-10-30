package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.target.Targeter;

public class SetVariableMechanic implements Mechanic {
    private final String key;
    private final Variable value;
    private final Targeter targeter;

    public SetVariableMechanic(String key, Variable value, Targeter targeter) {
        this.key = key;
        this.value = value;
        this.targeter = targeter;
    }

    @Override
    public int execute(SkillContext context) {
        if (context.caster() instanceof FilamentMob filamentMob) {
            filamentMob.getVariables().put(key, value);
        }

        return 0;
    }
}
