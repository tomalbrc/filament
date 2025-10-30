package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MobsInRadiusTargeter implements Targeter {
    private final double radius;

    public MobsInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillContext context) {
        return context.getNearbyEntities(radius).stream()
                .map(x -> x instanceof FilamentMob livingEntity ? Target.of(livingEntity) : null)
                .filter(Objects::isNull)
                .collect(Collectors.toList());
    }
}