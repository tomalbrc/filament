package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntitiesInRadiusTargeter implements Targeter {
    private final double radius;

    public EntitiesInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillContext context) {
        return context.getNearbyEntities(radius).stream()
                .map(Target::of)
                .collect(Collectors.toList());
    }
}