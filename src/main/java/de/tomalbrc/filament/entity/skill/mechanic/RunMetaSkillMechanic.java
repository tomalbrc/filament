package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.resources.ResourceLocation;

public class RunMetaSkillMechanic implements Mechanic {
    @Override
    public int execute(SkillContext context) {
        return 0;
    }


    @Override
    public ResourceLocation id() {
        return Mechanics.SKILL;
    }
}
