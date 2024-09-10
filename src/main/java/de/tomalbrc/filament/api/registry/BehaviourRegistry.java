package de.tomalbrc.filament.api.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.behaviours.block.*;
import de.tomalbrc.filament.behaviours.decoration.*;
import de.tomalbrc.filament.behaviours.item.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class BehaviourRegistry {
    private static final Map<ResourceLocation, BehaviourType<? extends Behaviour<?>, ?>> behaviourMap = new Object2ObjectOpenHashMap<>();

    public static <T extends Behaviour<E>,E> BehaviourType<T, E> registerBehaviour(String name, Class<T> type) {
        return registerBehaviour(ResourceLocation.fromNamespaceAndPath("filament", name), type);
    }

    public static <T extends Behaviour<E>,E> BehaviourType<T, E> registerBehaviour(ResourceLocation resourceLocation, Class<T> type) {
        Class<E> configType = inferConfigType(type);
        BehaviourType<T, E> behaviourType = new BehaviourType<T, E>(resourceLocation, type, configType);
        behaviourMap.put(resourceLocation, behaviourType);
        return behaviourType;
    }

    @SuppressWarnings("unchecked")
    private static <E> Class<E> inferConfigType(Class<?> behaviourClass) {
        // we assume the first generic parameter is the config class
        return (Class<E>) ((ParameterizedType) behaviourClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static <T extends Behaviour<E>, E> BehaviourType<T, E> getType(ResourceLocation key) {
        BehaviourType<?, ?> info = behaviourMap.get(key);
        if (info == null) {
            Filament.LOGGER.error("Could not find behaviour " + key);
        }
        return (BehaviourType<T, E>) behaviourMap.get(info.id());
    }

    public static <T extends Behaviour<E>, E> Behaviour<T> create(BehaviourType<T, E> type, E config) {
        BehaviourType<T, E> info = (BehaviourType<T, E>) behaviourMap.get(type);
        return (info != null) ? (Behaviour<T>) info.createInstance(config) : null;
    }

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

    public static final BehaviourType<Armor, Armor.ArmorConfig> ARMOR = registerBehaviour("armor", Armor.class);
    public static final BehaviourType<Cosmetic, Cosmetic.CosmeticConfig> COSMETIC = registerBehaviour("cosmetic", Cosmetic.class);
    public static final BehaviourType<Execute, Execute.ExecuteConfig> EXECUTE = registerBehaviour("execute", Execute.class);
    public static final BehaviourType<Food, Food.FoodConfig> FOOD = registerBehaviour("food", Food.class);
    public static final BehaviourType<Compostable, Compostable.CompostableConfig> COMPOSTABLE = registerBehaviour("compostable", Compostable.class);
    public static final BehaviourType<Fuel, Fuel.FuelConfig> FUEL = registerBehaviour("fuel", Fuel.class);
    public static final BehaviourType<Instrument, Instrument.InstrumentConfig> INSTRUMENT = registerBehaviour("instrument", Instrument.class);
    public static final BehaviourType<Shoot, Shoot.ShootConfig> SHOOT = registerBehaviour("shoot", Shoot.class);
    public static final BehaviourType<Trap, Trap.TrapConfig> TRAP = registerBehaviour("trap", Trap.class);

    public static final BehaviourType<Column, Column.ColumnConfig> COLUMN = registerBehaviour("column", Column.class);
    public static final BehaviourType<Count, Count.CountConfig> COUNT = registerBehaviour("count", Count.class);
    public static final BehaviourType<Directional, Directional.DirectionalConfig> DIRECTIONAL = registerBehaviour("directional", Directional.class);
    public static final BehaviourType<HorizontalDirectional, HorizontalDirectional.DirectionalConfig> HORIZONTAL_DIRECTIONAL = registerBehaviour("horizontal_directional", HorizontalDirectional.class);
    public static final BehaviourType<Powerlevel, Powerlevel.PowerlevelConfig> POWERLEVEL = registerBehaviour("powerlevel", Powerlevel.class);
    public static final BehaviourType<Slab, Slab.SlabConfig> SLAB = registerBehaviour("slab", Slab.class);
    public static final BehaviourType<Repeater, Repeater.RepeaterConfig> REPEATER = registerBehaviour("repeater", Repeater.class);
    public static final BehaviourType<Powersource, Powersource.PowersourceConfig> POWERSOURCE = registerBehaviour("powersource", Powersource.class);
    public static final BehaviourType<Strippable, Strippable.StrippableConfig> STRIPPABLE = registerBehaviour("strippable", Strippable.class);


    public static final BehaviourType<Animation, Animation.AnimationConfig> ANIMATION = registerBehaviour("animation", Animation.class);
    public static final BehaviourType<Container, Container.ContainerConfig> CONTAINER = registerBehaviour("container", Container.class);
    public static final BehaviourType<Lock, Lock.LockConfig> LOCK = registerBehaviour("lock", Lock.class);
    public static final BehaviourType<Seat, Seat.SeatConfig> SEAT = registerBehaviour("seat", Seat.class);
    public static final BehaviourType<Showcase, Showcase.ShowcaseConfig> SHOWCASE = registerBehaviour("showcase", Showcase.class);
}
