package de.tomalbrc.filament.datafixer;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import de.tomalbrc.filament.datafixer.fix.ChangeVersionAndRotationState;
import de.tomalbrc.filament.datafixer.schema.FilamentNamedspacedSchema;
import de.tomalbrc.filament.datafixer.schema.Version1;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataFix {
    @Nullable
    public static DataFixer DATA_FIXER;

    public static int VERSION = 2;

    public static float getVisualRotationYInDegrees(Direction direction, int rotation) {
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees((FilamentConfig.getInstance().alternativeBlockPlacement ? 0 : 180) + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }

    public static @NotNull DataFixer getDataFixer() {
        if (DATA_FIXER != null)
            return DATA_FIXER;

        DataFixerBuilder builder = new DataFixerBuilder(VERSION);
        builder.addSchema(1, Version1::new);
        Schema schema1 = builder.addSchema(2, FilamentNamedspacedSchema::new);
        builder.addFixer(ChangeVersionAndRotationState.create(schema1));
        DATA_FIXER = builder.build().fixer();

        return DATA_FIXER;
    }
}
