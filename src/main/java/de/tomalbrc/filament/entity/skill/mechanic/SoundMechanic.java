package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class SoundMechanic implements Mechanic {
    private final ResourceLocation sound;
    private final SoundSource source;
    private final float pitch;
    private final float volume;
    private final Targeter targeter;

    public SoundMechanic(ResourceLocation sound, SoundSource source, float pitch, float volume, Targeter targeter) {
        this.sound = sound;
        this.source = source;
        this.pitch = pitch;
        this.volume = volume;
        this.targeter = targeter;
    }

    @Override
    public int execute(SkillContext context) {
        List<Target> targets = targeter.find(context);
        for (Target target : targets) {
            target.level().playSound(null, context.caster(), SoundEvent.createVariableRangeEvent(sound), source, pitch, volume);
        }

        return 0;
    }
}