package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.AbstractTargeter;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class SelfTargeter extends AbstractTargeter {
    @Override
    public List<Target> find(SkillTree context) {
        return List.of(Target.of((Entity) context.caster));
    }
}