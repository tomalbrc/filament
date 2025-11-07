package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.ExecutionResult;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.AbstractMechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundMechanic extends AbstractMechanic {
    private final ResourceLocation sound;
    private final SoundSource source;
    private final float pitch;
    private final float volume;

    public SoundMechanic(ResourceLocation sound, SoundSource source, float pitch, float volume) {
        super();
        this.sound = sound;
        this.source = source;
        this.pitch = pitch;
        this.volume = volume;
    }

    @Override
    public ExecutionResult execute(SkillTree context) {
        if (context.getCurrentTargets() != null) {
            for (Target target : context.getCurrentTargets()) {
                target.level().playSound(null, context.caster(), SoundEvent.createVariableRangeEvent(sound), source, pitch, volume);
            }
        }

        return ExecutionResult.NULL;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.SOUND;
    }
}