package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillTree;
import net.minecraft.resources.ResourceLocation;

public interface Mechanic {
    int execute(SkillTree context);

    ResourceLocation id();

    default boolean isInline() {
        return true;
    }
}
