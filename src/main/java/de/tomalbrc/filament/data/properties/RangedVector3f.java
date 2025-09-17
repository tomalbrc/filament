package de.tomalbrc.filament.data.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.List;

public class RangedVector3f {
    private final RangedValue x;
    private final RangedValue y;
    private final RangedValue z;

    public RangedVector3f(RangedValue x, RangedValue y, RangedValue z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RangedVector3f() {
        this.x = new RangedValue(0);
        this.y = new RangedValue(0);
        this.z = new RangedValue(0);
    }

    public RangedValue getX() { return x; }
    public RangedValue getY() { return y; }
    public RangedValue getZ() { return z; }

    public Vector3f random(RandomSource random) {
        return new Vector3f(
                (float) x.randomValue(random),
                (float) y.randomValue(random),
                (float) z.randomValue(random)
        );
    }

    public static final Codec<RangedVector3f> CODEC =
            RangedValue.CODEC.listOf().comapFlatMap(
                    list -> {
                        if (list.size() != 3) {
                            return DataResult.error(() -> "Expected 3 elements for RangedVector3f, got " + list.size());
                        }
                        return DataResult.success(new RangedVector3f(list.get(0), list.get(1), list.get(2)));
                    },
                    vec -> List.of(vec.getX(), vec.getY(), vec.getZ())
            );
}