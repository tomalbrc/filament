package de.tomalbrc.filament.data.properties;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public class BlockStateMappedProperty<T> {
    private T value;
    private Map<String, T> valueMap;

    public BlockStateMappedProperty(T value) {
        this.value = value;
    }

    public BlockStateMappedProperty(Map<String, T> valueMap) {
        this.valueMap = valueMap;
    }

    public static <T> BlockStateMappedProperty<T> of(T value) {
        return new BlockStateMappedProperty<>(value);
    }

    public static <T> BlockStateMappedProperty<T> empty() {
        return new BlockStateMappedProperty<>(null);
    }

    public T getValue(BlockState blockState) {
        if (this.isMap()) {
            for (Map.Entry<String, T> entry : valueMap.entrySet()) {
                String[] props = entry.getKey().split(",");

                boolean missmatch = false;
                for (Property property : blockState.getProperties()) {
                    for (String propWithValue : props) {
                        String[] pair = propWithValue.split("=");
                        assert pair.length == 2;

                        if (property.getName().equals(pair[0]) && !property.getName(blockState.getValue(property)).equalsIgnoreCase(pair[1])) {
                            missmatch = true;
                        }
                    }
                }

                if (!missmatch) {
                    return entry.getValue();
                }
            }
        }

        return this.value;
    }

    public T getOrDefault(BlockState key, T def) {
        var val = this.getValue(key);
        return val == null ? def : val;
    }

    public boolean isMap() {
        return this.valueMap != null && !this.valueMap.isEmpty();
    }

    public T getRawValue() {
        return this.value;
    }
}
