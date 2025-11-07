package de.tomalbrc.filament.entity.skill.target;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.EntityRefTable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractTargeter implements Targeter {
    @SerializedName(value = "sort", alternate = {"sorttype"})
    protected Sorting sorting = Sorting.NONE;
    @SerializedName(value = "skipTargetsUpToIndex", alternate = {"skip-targets-up-to-index", "stuti"})
    protected int skipTargetsUpToIndex = 0;
    protected int limit = 0;

    @Override
    public List<Target> sort(Level level, @Nullable EntityRefTable threatTable, Vec3 origin, List<Target> targets) {
        if (targets == null || targets.isEmpty() || sorting == Sorting.NONE)
            return targets;

        return switch (sorting) {
            case NONE -> null;
            case RANDOM -> {
                List<Target> shuffled = new ArrayList<>(targets);
                Collections.shuffle(shuffled);
                yield shuffled;
            }

            case NEAREST -> targets.stream()
                    .sorted(Comparator.comparingDouble(t -> t.getPosition().distanceTo(origin)))
                    .toList();

            case FURTHEST -> targets.stream()
                    .sorted(Comparator.comparingDouble((Target t) -> t.getPosition().distanceTo(origin)).reversed())
                    .toList();

            case HIGHEST_HEALTH -> targets.stream()
                    .sorted(Comparator.comparingDouble((Target t) -> t.getEntity().asLivingEntity().getHealth()).reversed())
                    .toList();

            case LOWEST_HEALTH -> targets.stream()
                    .sorted(Comparator.comparingDouble(t -> t.getEntity().asLivingEntity().getHealth()))
                    .toList();

            case HIGHEST_THREAT -> {
                if (threatTable == null) yield targets;
                yield targets.stream()
                        .sorted(Comparator.comparingDouble((Target t) -> threatTable.get(t.getEntity())).reversed())
                        .toList();
            }

            case LOWEST_THREAT -> {
                if (threatTable == null) yield targets;
                yield targets.stream()
                        .sorted(Comparator.comparingDouble(
                                t -> threatTable.get(t.getEntity().getUUID())))
                        .toList();
            }
        };
    }
}
