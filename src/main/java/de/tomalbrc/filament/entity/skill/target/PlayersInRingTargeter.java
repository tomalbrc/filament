package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayersInRingTargeter implements Targeter {
    private final double innerRadius;
    private final double outerRadius;

    public PlayersInRingTargeter(double innerRadius, double outerRadius) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    @Override
    public List<Target> find(SkillContext context) {
        return context.getNearbyEntities(outerRadius).stream()
                .filter(entity -> entity instanceof Player)
                .filter(entity -> {
                    double distance = entity.position().distanceTo(context.caster().position());
                    return distance >= innerRadius && distance <= outerRadius;
                })
                .map(Target::of)
                .collect(Collectors.toList());
    }
}