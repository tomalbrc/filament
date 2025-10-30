package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.resources.ResourceLocation;

public interface Mechanic {
    int execute(SkillContext context);

    ResourceLocation id();
}
