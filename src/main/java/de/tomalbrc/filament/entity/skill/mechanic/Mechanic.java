package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.SkillTree;
import net.minecraft.resources.ResourceLocation;

public interface Mechanic {
    ExecutionResult execute(SkillTree context);

    ResourceLocation id();
}
