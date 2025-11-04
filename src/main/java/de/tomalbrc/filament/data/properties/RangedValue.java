package de.tomalbrc.filament.data.properties;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.RandomSource;

public class RangedValue {
    private final double min;
    private final double max;

    public RangedValue(String input) {
        if (input.contains("..")) {
            String[] parts = input.split("\\.\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid range: " + input);
            }
            double min = Double.parseDouble(parts[0]);
            double max = Double.parseDouble(parts[1]);
            if (min > max) {
                this.min = max;
                this.max = min;
            } else {
                this.min = min;
                this.max = max;
            }
        } else {
            double value = Double.parseDouble(input);
            this.min = value;
            this.max = value;
        }
    }

    public RangedValue(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.min = min;
        this.max = max;
    }

    public RangedValue(double fixed) {
        this.min = fixed;
        this.max = fixed;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }

    public double randomValue(RandomSource randomSource) {
        return isFixed() ? min : (min + randomSource.nextDouble() * (max - min));
    }

    public boolean isFixed() {
        return min == max;
    }

    public boolean isInRange(double value) {
        return isFixed() ? value == min : value >= min && value <= max;
    }

    public static final Codec<RangedValue> CODEC = Codec.either(
            Codec.DOUBLE,
            Codec.STRING
    ).flatXmap(
            either -> {
                try {
                    if (either.left().isPresent()) {
                        double value = either.left().get();
                        return DataResult.success(new RangedValue(value, value));
                    } else {
                        return DataResult.success(new RangedValue(either.right().get()));
                    }
                } catch (Exception ex) {
                    return DataResult.error(() -> "Invalid RangedValue: " + ex.getMessage());
                }
            },
            ranged -> {
                if (ranged.isFixed()) {
                    return DataResult.success(Either.left(ranged.getMin()));
                } else {
                    return DataResult.success(Either.right(ranged.getMin() + ".." + ranged.getMax()));
                }
            }
    );
}