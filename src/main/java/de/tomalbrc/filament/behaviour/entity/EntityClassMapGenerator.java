package de.tomalbrc.filament.behaviour.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class EntityClassMapGenerator {
    private static final Map<Identifier, Class<?>> ENTITY_CLASS_MAP = new HashMap<>();

    // not sure if there is a better way but this works for now
    static {
        Field[] fields = EntityType.class.getDeclaredFields();

        for (Field field : fields) {
            if (EntityType.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    EntityType<?> entityType = (EntityType<?>) field.get(null);
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                        if (actualType instanceof Class<?>) {
                            ENTITY_CLASS_MAP.put(id, (Class<?>) actualType);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error extracting entity class from field: " + field.getName(), e);
                }
            }
        }
    }

    public static Class<?> getEntityClass(Identifier id) {
        return ENTITY_CLASS_MAP.get(id);
    }

    public static Map<Identifier, Class<?>> getAllEntityClasses() {
        return ENTITY_CLASS_MAP;
    }
}

