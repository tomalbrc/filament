package de.tomalbrc.filament.api.behaviour;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;

public record BehaviourType<T extends Behaviour<C>, C>(ResourceLocation id, Class<T> type, Class<C> configType) {
    public T createInstance(C object) {
        try {
            return type.getDeclaredConstructor(this.configType()).newInstance(object);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
