package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.MobSkills;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
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
    public int execute(SkillTree tree) {
        var vars = switch (scope) {
            case GLOBAL -> MobSkills.getGlobalVariables();
            case WORLD -> MobSkills.getWorldVariables(tree.caster.level().dimension());
            case SKILL -> tree.vars();
            case CASTER -> tree.caster.getVariables();
            case TARGET -> null;
        };

        if (scope == Variable.Scope.TARGET && tree.getCurrentTargets() != null) {
            for (Target target : tree.getCurrentTargets()) {
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
