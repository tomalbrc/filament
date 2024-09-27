package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.behaviour.block.*;
import de.tomalbrc.filament.behaviour.decoration.*;
import de.tomalbrc.filament.behaviour.item.*;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Behaviours {
    // Item
    public static final BehaviourType<Armor, Armor.Config> ARMOR = registerBehaviour("armor", Armor.class);
    public static final BehaviourType<Cosmetic, Cosmetic.Config> COSMETIC = registerBehaviour("cosmetic", Cosmetic.class);
    public static final BehaviourType<Execute, Execute.ExecuteConfig> EXECUTE = registerBehaviour("execute", Execute.class);
    public static final BehaviourType<Food, Food.FoodConfig> FOOD = registerBehaviour("food", Food.class);
    public static final BehaviourType<Compostable, Compostable.CompostableConfig> COMPOSTABLE = registerBehaviour("compostable", Compostable.class);
    public static final BehaviourType<Fuel, Fuel.Config> FUEL = registerBehaviour("fuel", Fuel.class);
    public static final BehaviourType<Instrument, Instrument.Config> INSTRUMENT = registerBehaviour("instrument", Instrument.class);
    public static final BehaviourType<Shoot, Shoot.ShootConfig> SHOOT = registerBehaviour("shoot", Shoot.class);
    public static final BehaviourType<Stripper, Stripper.Config> STRIPPER = registerBehaviour("stripper", Stripper.class);
    public static final BehaviourType<Trap, Trap.Config> TRAP = registerBehaviour("trap", Trap.class);
    public static final BehaviourType<BannerPattern, BannerPattern.Config> BANNER_PATTERN = registerBehaviour("banner_pattern", BannerPattern.class);

    // Block
    public static final BehaviourType<CanSurvive, CanSurvive.Config> CAN_SURVIVE = registerBehaviour("can_survive", CanSurvive.class);
    public static final BehaviourType<Axis, Axis.Config> AXIS = registerBehaviour("axis", Axis.class);
    public static final BehaviourType<Count, Count.CountConfig> COUNT = registerBehaviour("count", Count.class);
    public static final BehaviourType<Crop, Crop.Config> CROP = registerBehaviour("crop", Crop.class);
    public static final BehaviourType<Facing, Facing.Config> FACING = registerBehaviour("facing", Facing.class);
    public static final BehaviourType<HorizontalFacing, HorizontalFacing.Config> HORIZONTAL_FACING = registerBehaviour("horizontal_facing", HorizontalFacing.class);
    public static final BehaviourType<Powerlevel, Powerlevel.PowerlevelConfig> POWERLEVEL = registerBehaviour("powerlevel", Powerlevel.class);
    public static final BehaviourType<Slab, Slab.SlabConfig> SLAB = registerBehaviour("slab", Slab.class);
    public static final BehaviourType<Repeater, Repeater.RepeaterConfig> REPEATER = registerBehaviour("repeater", Repeater.class);
    public static final BehaviourType<Powersource, Powersource.PowersourceConfig> POWERSOURCE = registerBehaviour("powersource", Powersource.class);
    public static final BehaviourType<Strippable, Strippable.StrippableConfig> STRIPPABLE = registerBehaviour("strippable", Strippable.class);
    public static final BehaviourType<DropXp, DropXp.Config> DROP_XP = registerBehaviour("drop_xp", DropXp.class);

    // Decoration
    public static final BehaviourType<Animation, Animation.AnimationConfig> ANIMATION = registerBehaviour("animation", Animation.class);
    public static final BehaviourType<Container, Container.ContainerConfig> CONTAINER = registerBehaviour("container", Container.class);
    public static final BehaviourType<Lock, Lock.LockConfig> LOCK = registerBehaviour("lock", Lock.class);
    public static final BehaviourType<Seat, Seat.SeatConfig> SEAT = registerBehaviour("seat", Seat.class);
    public static final BehaviourType<Showcase, Showcase.ShowcaseConfig> SHOWCASE = registerBehaviour("showcase", Showcase.class);

    private static <T extends Behaviour<E>,E> BehaviourType<T, E> registerBehaviour(String name, Class<T> type) {
        return BehaviourRegistry.registerBehaviour(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), type);
    }

    public static void init() {
    }
}
