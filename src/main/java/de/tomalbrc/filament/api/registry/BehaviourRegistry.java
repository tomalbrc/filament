package de.tomalbrc.filament.api.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.behaviours.block.*;
import de.tomalbrc.filament.behaviours.decoration.*;
import de.tomalbrc.filament.behaviours.item.*;
import de.tomalbrc.filament.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

public class BehaviourRegistry {
    private static final Map<ResourceLocation, BehaviourType> behaviourMap = new Object2ObjectOpenHashMap<>();

    public static void registerBehaviour(ResourceLocation resourceLocation, Class<? extends Behaviour<?>> type) {
        behaviourMap.put(resourceLocation, new BehaviourType(resourceLocation, type, o -> {
            try {
                return type.getDeclaredConstructor(Behaviour.getConfigType(type)).newInstance(o);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static Type getConfigType(ResourceLocation key) {
        BehaviourType info = behaviourMap.get(key);
        if (info == null) {
            Filament.LOGGER.error("Could not find behaviour " + key);
        }
        return Behaviour.getConfigType(info.type);
    }

    public static Behaviour<?> create(ResourceLocation key, Object config) {
        BehaviourType info = behaviourMap.get(key);
        return (info != null) ? info.createInstance(config) : null;
    }

    public record BehaviourType(ResourceLocation resourceLocation, Class<? extends Behaviour<?>> type, Function<Object, Behaviour> function) {
        public Behaviour createInstance(Object object) {
            return function.apply(object);
        }
    }

    static {
        registerBehaviour(Constants.Behaviours.ARMOR, Armor.class);
        registerBehaviour(Constants.Behaviours.COSMETIC, Cosmetic.class);
        registerBehaviour(Constants.Behaviours.EXECUTE, Execute.class);
        registerBehaviour(Constants.Behaviours.FOOD, Food.class);
        registerBehaviour(Constants.Behaviours.COMPOSTABLE, Compostable.class);
        registerBehaviour(Constants.Behaviours.FUEL, Fuel.class);
        registerBehaviour(Constants.Behaviours.INSTRUMENT, Instrument.class);
        registerBehaviour(Constants.Behaviours.SHOOT, Shoot.class);
        registerBehaviour(Constants.Behaviours.TRAP, Trap.class);

        registerBehaviour(Constants.Behaviours.COLUMN, Column.class);
        registerBehaviour(Constants.Behaviours.COUNT, Column.class);
        //registerBehaviour(Constants.Behaviours.DIRECTIONAL_POWERED, DirectionalPowered.class);
        //registerBehaviour(Constants.Behaviours.DIRECTIONAL, .class);
        registerBehaviour(Constants.Behaviours.POWERLEVEL, Powerlevel.class);
        registerBehaviour(Constants.Behaviours.SLAB, Slab.class);
        registerBehaviour(Constants.Behaviours.REPEATER, Repeater.class);
        registerBehaviour(Constants.Behaviours.POWERSOURCE, Powersource.class);
        registerBehaviour(Constants.Behaviours.STRIPPABLE, Strippable.class);

        registerBehaviour(Constants.Behaviours.ANIMATION, Animation.class);
        registerBehaviour(Constants.Behaviours.CONTAINER, Container.class);
        registerBehaviour(Constants.Behaviours.LOCK, Lock.class);
        registerBehaviour(Constants.Behaviours.SEAT, Seat.class);
        registerBehaviour(Constants.Behaviours.SHOWCASE, Showcase.class);
    }
}
