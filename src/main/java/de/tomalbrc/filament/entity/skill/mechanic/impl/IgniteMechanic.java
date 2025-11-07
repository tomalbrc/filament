package de.tomalbrc.filament.entity.skill.mechanic.impl;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;

public class IgniteMechanic implements Mechanic {
    @SerializedName(value = "ticks", alternate = {"t", "d", "duration"})
    private final int ticks;

    public IgniteMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public ExecutionResult execute(SkillTree context) {
        if (context.getCurrentTargets() != null) {
            for (Target target : context.getCurrentTargets()) {
                target.getEntity().igniteForTicks(ticks);
            }
        }

        return ExecutionResult.NULL;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.IGNITE;
    }
}
