package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.MobSkills;
import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import net.minecraft.resources.ResourceLocation;

public class SetVariableMechanic implements Mechanic {
    private final String key;
    private final Variable value;
    private final Variable.Scope scope;

    public SetVariableMechanic(String key, Variable value, Variable.Scope scope) {
        this.key = key;
        this.value = value;
        this.scope = scope;
    }

    @Override
    public int execute(SkillContext context) {
        var vars = switch (scope) {
            case GLOBAL -> MobSkills.getGlobalVariables();
            case WORLD -> MobSkills.getWorldVariables(context.level().dimension());
            case SKILL -> context.vars();
            case CASTER -> context.caster().getVariables();
            case TARGET -> null;
        };

        if (scope == Variable.Scope.TARGET && context.targets() != null) {
            for (Target target : context.targets()) {
                if (target.isEntity()) {
                    vars = MobSkills.getEntityVariables(target.getEntity().getUUID());
                    vars.put(key, value);
                }
            }
        } else if (vars != null) {
            vars.put(key, value);
        }

        return 0;
    }


    @Override
    public ResourceLocation id() {
        return Mechanics.SET_VARIABLE;
    }
}
