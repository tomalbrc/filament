package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.SkillContext;

import java.util.List;
import java.util.stream.Collectors;

public class PlayersInWorldTargeter implements Targeter {
    @Override
    public List<Target> find(SkillContext context) {
        return context.level().players().stream()
                .map(Target::of)
                .collect(Collectors.toList());
    }
}