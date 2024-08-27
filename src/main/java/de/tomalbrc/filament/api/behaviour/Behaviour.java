package de.tomalbrc.filament.api.behaviour;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface Behaviour<T> {
    T getConfig();

    static Class<?> getConfigType(Class<? extends Behaviour<?>> behaviour) {
        Type[] genericInterfaces = behaviour.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                return (Class<?>)parameterizedType.getActualTypeArguments()[0];
            }
        }
        return null;
    }
}
