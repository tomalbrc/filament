package de.tomalbrc.filament.api.registry;

import com.google.common.reflect.TypeToken;
import de.tomalbrc.filament.data.behaviours.block.Powersource;
import de.tomalbrc.filament.data.behaviours.block.Repeater;
import de.tomalbrc.filament.data.behaviours.block.Strippable;
import de.tomalbrc.filament.data.behaviours.decoration.*;
import de.tomalbrc.filament.data.behaviours.item.*;
import de.tomalbrc.filament.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BehaviourRegistry {
    private static final Map<ResourceLocation, Type> behaviourMap = new Object2ObjectOpenHashMap<>();

    public static void init() {
        registerBuiltin();
    }

    private static void registerBuiltin() {
        registerBehaviour(Constants.Behaviours.ARMOR, Armor.class);
        registerBehaviour(Constants.Behaviours.COSMETIC, Cosmetic.class);
        registerBehaviour(Constants.Behaviours.EXECUTE, Execute.class);
        registerBehaviour(Constants.Behaviours.FOOD, Food.class);
        registerBehaviour(Constants.Behaviours.FUEL, Fuel.class);
        registerBehaviour(Constants.Behaviours.INSTRUMENT, Instrument.class);
        registerBehaviour(Constants.Behaviours.SHOOT, Shoot.class);
        registerBehaviour(Constants.Behaviours.TRAP, Trap.class);

        registerBehaviour(Constants.Behaviours.REPEATER, Repeater.class);
        registerBehaviour(Constants.Behaviours.POWERSOURCE, Powersource.class);
        registerBehaviour(Constants.Behaviours.STRIPPABLE, Strippable.class);

        registerBehaviour(Constants.Behaviours.ANIMATION, Animation.class);
        registerBehaviour(Constants.Behaviours.CONTAINER, Container.class);
        registerBehaviour(Constants.Behaviours.LOCK, Lock.class);
        registerBehaviour(Constants.Behaviours.SEAT, new TypeToken<List<Seat>>() {}.getType());
        registerBehaviour(Constants.Behaviours.SHOWCASE, new TypeToken<List<Showcase>>() {}.getType());
    }

    public static void registerBehaviour(ResourceLocation resourceLocation, Type behaviour) {
        behaviourMap.put(resourceLocation, behaviour);
    }

    public static Type get(ResourceLocation key) {
        return behaviourMap.get(key);
    }
}
