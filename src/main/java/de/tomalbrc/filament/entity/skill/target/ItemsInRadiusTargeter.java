package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemsInRadiusTargeter implements Targeter {
    private final double radius;

    public ItemsInRadiusTargeter(double radius) {
        this.radius = radius;
    }

    @Override
    public List<Target> find(SkillContext context) {
        return context.getNearbyEntities(radius).stream()
                .map(x -> x instanceof ItemEntity entity ? Target.of(entity) : null)
                .filter(Objects::isNull)
                .collect(Collectors.toList());
    }
}