package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.behaviour.block.*;
import de.tomalbrc.filament.behaviour.decoration.*;
import de.tomalbrc.filament.behaviour.entity.goal.*;
import de.tomalbrc.filament.behaviour.entity.target.DefendVillageGoal;
import de.tomalbrc.filament.behaviour.entity.target.HurtByTargetGoal;
import de.tomalbrc.filament.behaviour.entity.target.NearestAttackableTargetGoal;
import de.tomalbrc.filament.behaviour.item.*;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Behaviours {
    // Item
    public static final BehaviourType<Cosmetic, Cosmetic.Config> COSMETIC = registerBehaviour("cosmetic", Cosmetic.class);
    public static final BehaviourType<Food, Food.FoodConfig> FOOD = registerBehaviour("food", Food.class);
    public static final BehaviourType<Compostable, Compostable.CompostableConfig> COMPOSTABLE = registerBehaviour("compostable", Compostable.class);
    public static final BehaviourType<Fuel, Fuel.Config> FUEL = registerBehaviour("fuel", Fuel.class);
    public static final BehaviourType<Hoe, Hoe.Config> HOE = registerBehaviour("hoe", Hoe.class);
    public static final BehaviourType<Instrument, Instrument.Config> INSTRUMENT = registerBehaviour("instrument", Instrument.class);
    public static final BehaviourType<Shoot, Shoot.ShootConfig> SHOOT = registerBehaviour("shoot", Shoot.class);
    public static final BehaviourType<Bow, Bow.Config> BOW = registerBehaviour("bow", Bow.class);
    public static final BehaviourType<Crossbow, Crossbow.Config> CROSSBOW = registerBehaviour("crossbow", Crossbow.class);
    public static final BehaviourType<Shield, Shield.Config> SHIELD = registerBehaviour("shield", Shield.class);
    public static final BehaviourType<FishingRod, FishingRod.Config> FISHING_ROD = registerBehaviour("fishing_rod", FishingRod.class);
    public static final BehaviourType<Mace, Mace.Config> MACE = registerBehaviour("mace", Mace.class);
    public static final BehaviourType<Shears, Shears.Config> SHEARS = registerBehaviour("shears", Shears.class);
    public static final BehaviourType<Shovel, Shovel.Config> SHOVEL = registerBehaviour("shovel", Shovel.class);
    public static final BehaviourType<Stripper, Stripper.Config> STRIPPER = registerBehaviour("stripper", Stripper.class);
    public static final BehaviourType<Trap, Trap.Config> TRAP = registerBehaviour("trap", Trap.class);
    public static final BehaviourType<Trident, Trident.Config> TRIDENT = registerBehaviour("trident", Trident.class);
    public static final BehaviourType<BannerPattern, BannerPattern.Config> BANNER_PATTERN = registerBehaviour("banner_pattern", BannerPattern.class);
    public static final BehaviourType<VillagerFood, VillagerFood.Config> VILLAGER_FOOD = registerBehaviour("villager_food", VillagerFood.class);

    // Item + Block
    public static final BehaviourType<Execute, Execute.Config> EXECUTE = registerBehaviour("execute", Execute.class);

    // Block
    public static final BehaviourType<CanSurvive, CanSurvive.Config> CAN_SURVIVE = registerBehaviour("can_survive", CanSurvive.class);
    public static final BehaviourType<Axis, Axis.Config> AXIS = registerBehaviour("axis", Axis.class);
    public static final BehaviourType<Count, Count.CountConfig> COUNT = registerBehaviour("count", Count.class);
    public static final BehaviourType<Crop, Crop.Config> CROP = registerBehaviour("crop", Crop.class);
    public static final BehaviourType<Budding, Budding.Config> BUDDING = registerBehaviour("budding", Budding.class);
    public static final BehaviourType<Facing, Facing.Config> FACING = registerBehaviour("facing", Facing.class);
    public static final BehaviourType<FallingBlock, FallingBlock.Config> FALLING_BLOCK = registerBehaviour("falling_block", FallingBlock.class);
    public static final BehaviourType<Furnace, Furnace.Config> FURNACE = registerBehaviour("furnace", Furnace.class);
    public static final BehaviourType<GrassSpread, GrassSpread.Config> GRASS_SPREAD = registerBehaviour("grass_spread", GrassSpread.class);
    public static final BehaviourType<Hopper, Hopper.Config> HOPPER = registerBehaviour("hopper", Hopper.class);
    public static final BehaviourType<HorizontalFacing, HorizontalFacing.Config> HORIZONTAL_FACING = registerBehaviour("horizontal_facing", HorizontalFacing.class);
    public static final BehaviourType<Powerlevel, Powerlevel.PowerlevelConfig> POWERLEVEL = registerBehaviour("powerlevel", Powerlevel.class);
    public static final BehaviourType<Slab, Slab.SlabConfig> SLAB = registerBehaviour("slab", Slab.class);
    public static final BehaviourType<Trapdoor, Trapdoor.Config> TRAPDOOR = registerBehaviour("trapdoor", Trapdoor.class);
    public static final BehaviourType<Door, Door.Config> DOOR = registerBehaviour("door", Door.class);
    public static final BehaviourType<Sapling, Sapling.Config> SAPLING = registerBehaviour("sapling", Sapling.class);
    public static final BehaviourType<SimpleWaterloggable, SimpleWaterloggable.Config> SIMPLE_WATERLOGGABLE = registerBehaviour("simple_waterloggable", SimpleWaterloggable.class);
    public static final BehaviourType<Repeater, Repeater.RepeaterConfig> REPEATER = registerBehaviour("repeater", Repeater.class);
    public static final BehaviourType<Powersource, Powersource.PowersourceConfig> POWERSOURCE = registerBehaviour("powersource", Powersource.class);
    public static final BehaviourType<Strippable, Strippable.StrippableConfig> STRIPPABLE = registerBehaviour("strippable", Strippable.class);
    public static final BehaviourType<Tnt, Tnt.Config> TNT = registerBehaviour("tnt", Tnt.class);
    public static final BehaviourType<DropXp, DropXp.Config> DROP_XP = registerBehaviour("drop_xp", DropXp.class);
    public static final BehaviourType<Oxidizable, Oxidizable.Config> OXIDIZABLE = registerBehaviour("oxidizable", Oxidizable.class);

    // Decoration
    public static final BehaviourType<Animation, Animation.AnimationConfig> ANIMATION = registerBehaviour("animation", Animation.class);
    public static final BehaviourType<Bed, Bed.Config> BED = registerBehaviour("bed", Bed.class);
    public static final BehaviourType<Container, Container.ContainerConfig> CONTAINER = registerBehaviour("container", Container.class);
    public static final BehaviourType<Lock, Lock.LockConfig> LOCK = registerBehaviour("lock", Lock.class); // this only exists for backwards compatibility
    public static final BehaviourType<Lock, Lock.LockConfig> INTERACT_EXECUTE = registerBehaviour("interact_execute", Lock.class);
    public static final BehaviourType<Seat, Seat.SeatConfig> SEAT = registerBehaviour("seat", Seat.class);
    public static final BehaviourType<Showcase, Showcase.ShowcaseConfig> SHOWCASE = registerBehaviour("showcase", Showcase.class);
    public static final BehaviourType<Lamp, Lamp.Config> LAMP = registerBehaviour("lamp", Lamp.class);

    // Entity
    // goals
    public static final BehaviourType<FloatGoal, FloatGoal.Config> FLOAT_GOAL = registerBehaviour("float_goal", FloatGoal.class);
    public static final BehaviourType<RemoveBlockGoal, RemoveBlockGoal.Config> REMOVE_BLOCK_GOAL = registerBehaviour("remove_block_goal", RemoveBlockGoal.class);
    public static final BehaviourType<LookAtMobGoal, LookAtMobGoal.Config> LOOK_AT_MOB_GOAL = registerBehaviour("look_at_mob_goal", LookAtMobGoal.class);
    public static final BehaviourType<RandomLookAroundGoal, RandomLookAroundGoal.Config> RANDOM_LOOK_AROUND_GOAL = registerBehaviour("random_look_around_goal", RandomLookAroundGoal.class);
    public static final BehaviourType<MeleeAttackGoal, MeleeAttackGoal.Config> MELEE_ATTACK_GOAL = registerBehaviour("melee_attack_goal", MeleeAttackGoal.class);
    public static final BehaviourType<MoveThroughVillageGoal, MoveThroughVillageGoal.Config> MOVE_THROUGH_VILLAGE_GOAL = registerBehaviour("move_through_village_goal", MoveThroughVillageGoal.class);
    public static final BehaviourType<WaterAvoidingRandomStrollGoal, WaterAvoidingRandomStrollGoal.Config> WATER_AVOIDING_RANDOM_STROLL_GOAL = registerBehaviour("water_avoiding_random_stroll_goal", WaterAvoidingRandomStrollGoal.class);
    // target goals
    public static final BehaviourType<DefendVillageGoal, DefendVillageGoal.Config> DEFEND_VILLAGE_GOAL = registerBehaviour("defend_village_goal", DefendVillageGoal.class);
    public static final BehaviourType<HurtByTargetGoal, HurtByTargetGoal.Config> HURT_BY_TARGET_GOAL = registerBehaviour("hurt_by_target_goal", HurtByTargetGoal.class);
    public static final BehaviourType<NearestAttackableTargetGoal, NearestAttackableTargetGoal.Config> NEAREST_ATTACKABLE_TARGET_GOAL = registerBehaviour("nearest_attackable_target_goal", NearestAttackableTargetGoal.class);


    private static <T extends Behaviour<E>,E> BehaviourType<T, E> registerBehaviour(String name, Class<T> type) {
        return BehaviourRegistry.registerBehaviour(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), type);
    }

    public static void register() {
    }

    // for compat. with older mods
    public static void init() {}
}
