package de.tomalbrc.filament.entity.skill.target;

import de.tomalbrc.filament.entity.skill.EntityRefTable;
import de.tomalbrc.filament.entity.skill.SkillTree;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Targeter {
    List<Target> find(SkillTree context);

    List<Target> sort(Level level, @Nullable EntityRefTable threatTable, Vec3 origin, List<Target> targets);

    enum Sorting {
        NONE,
        RANDOM,
        NEAREST,
        FURTHEST,
        // entity only
        HIGHEST_HEALTH,
        LOWEST_HEALTH,
        HIGHEST_THREAT,
        LOWEST_THREAT
    }
}