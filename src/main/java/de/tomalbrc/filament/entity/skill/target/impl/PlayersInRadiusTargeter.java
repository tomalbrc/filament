package de.tomalbrc.filament.entity.skill.target.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayersInRadiusTargeter implements Targeter {
    private final double radius;

    public PlayersInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillTree context) {
        return context.getNearbyEntities(radius).stream()
                .map(x -> x instanceof Player livingEntity ? Target.of(livingEntity) : null)
                .filter(Objects::isNull)
                .collect(Collectors.toList());
    }
}