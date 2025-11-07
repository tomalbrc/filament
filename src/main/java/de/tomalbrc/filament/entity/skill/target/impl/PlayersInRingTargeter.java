package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.AbstractTargeter;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayersInRingTargeter extends AbstractTargeter {
    private final double innerRadius;
    private final double outerRadius;

    public PlayersInRingTargeter(double innerRadius, double outerRadius) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    @Override
    public List<Target> find(SkillTree context) {
        return context.getNearbyEntities(outerRadius).stream()
                .filter(entity -> entity instanceof Player)
                .filter(entity -> {
                    double distance = entity.position().distanceTo(context.caster.position());
                    return distance >= innerRadius && distance <= outerRadius;
                })
                .map(Target::of)
                .collect(Collectors.toList());
    }
}