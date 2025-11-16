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
    public static final BehaviourType<BannerPattern, BannerPattern.Config> BANNER_PATTERN = registerBehaviour("banner_pattern", BannerPattern.class); // TODO: deprecated?
    public static final BehaviourType<Bow, Bow.Config> BOW = registerBehaviour("bow", Bow.class);
    public static final BehaviourType<Compostable, Compostable.CompostableConfig> COMPOSTABLE = registerBehaviour("compostable", Compostable.class);
    public static final BehaviourType<Cosmetic, Cosmetic.Config> COSMETIC = registerBehaviour("cosmetic", Cosmetic.class);
    public static final BehaviourType<Crossbow, Crossbow.Config> CROSSBOW = registerBehaviour("crossbow", Crossbow.class);
    public static final BehaviourType<ExecuteAttackItem, ExecuteAttackItem.Config> ITEM_ATTACK_EXECUTE = registerBehaviour("item_attack_execute", ExecuteAttackItem.class);
    public static final BehaviourType<ExecuteInteractItem, ExecuteInteractItem.Config> ITEM_INTERACT_EXECUTE = registerBehaviour("item_interact_execute", ExecuteInteractItem.class);
    public static final BehaviourType<FishingRod, FishingRod.Config> FISHING_ROD = registerBehaviour("fishing_rod", FishingRod.class);
    public static final BehaviourType<Fuel, Fuel.Config> FUEL = registerBehaviour("fuel", Fuel.class);
    public static final BehaviourType<Hoe, Hoe.Config> HOE = registerBehaviour("hoe", Hoe.class);
    public static final BehaviourType<Instrument, Instrument.Config> INSTRUMENT = registerBehaviour("instrument", Instrument.class);
    public static final BehaviourType<Mace, Mace.Config> MACE = registerBehaviour("mace", Mace.class);
    public static final BehaviourType<PlaceOnWater, PlaceOnWater.Config> PLACE_ON_WATER = registerBehaviour("place_on_water", PlaceOnWater.class);
    public static final BehaviourType<Shoot, Shoot.Config> SHOOT = registerBehaviour("shoot", Shoot.class);
    public static final BehaviourType<Shield, Shield.Config> SHIELD = registerBehaviour("shield", Shield.class);
    public static final BehaviourType<Shears, Shears.Config> SHEARS = registerBehaviour("shears", Shears.class);
    public static final BehaviourType<Shovel, Shovel.Config> SHOVEL = registerBehaviour("shovel", Shovel.class);
    public static final BehaviourType<Snowball, Snowball.Config> SNOWBALL = registerBehaviour("snowball", Snowball.class);
    public static final BehaviourType<Stripper, Stripper.Config> STRIPPER = registerBehaviour("stripper", Stripper.class);
    public static final BehaviourType<Trap, Trap.Config> TRAP = registerBehaviour("trap", Trap.class);
    public static final BehaviourType<Trident, Trident.Config> TRIDENT = registerBehaviour("trident", Trident.class);
    public static final BehaviourType<VillagerFood, VillagerFood.Config> VILLAGER_FOOD = registerBehaviour("villager_food", VillagerFood.class);
    public static final BehaviourType<Wax, Wax.Config> WAX = registerBehaviour("wax", Wax.class);

    // Item + Block
    @Deprecated(forRemoval = true)
    public static final BehaviourType<Execute, Execute.Config> EXECUTE = registerBehaviour("execute", Execute.class);

    // Block
    public static final BehaviourType<AreaExecute, AreaExecute.Config> AREA_EXECUTE = registerBehaviour("area_execute", AreaExecute.class);
    public static final BehaviourType<Axis, Axis.Config> AXIS = registerBehaviour("axis", Axis.class);
    public static final BehaviourType<Bouncy, Bouncy.Config> BOUNCY = registerBehaviour("bouncy", Bouncy.class);
    public static final BehaviourType<Budding, Budding.Config> BUDDING = registerBehaviour("budding", Budding.class);
    public static final BehaviourType<Button, Button.Config> BUTTON = registerBehaviour("button", Button.class);
    public static final BehaviourType<CanSurvive, CanSurvive.Config> CAN_SURVIVE = registerBehaviour("can_survive", CanSurvive.class);
    public static final BehaviourType<Count, Count.Config> COUNT = registerBehaviour("count", Count.class);
    public static final BehaviourType<Crop, Crop.Config> CROP = registerBehaviour("crop", Crop.class);
    public static final BehaviourType<Door, Door.Config> DOOR = registerBehaviour("door", Door.class);
    public static final BehaviourType<DropXp, DropXp.Config> DROP_XP = registerBehaviour("drop_xp", DropXp.class);
    public static final BehaviourType<ExecuteInteractBlock, ExecuteInteractBlock.Config> BLOCK_INTERACT_EXECUTE = registerBehaviour("block_interact_execute", ExecuteInteractBlock.class);
    public static final BehaviourType<ExecuteAttackBlock, ExecuteAttackBlock.Config> BLOCK_ATTACK_EXECUTE = registerBehaviour("block_attack_execute", ExecuteAttackBlock.class);
    public static final BehaviourType<Facing, Facing.Config> FACING = registerBehaviour("facing", Facing.class);
    public static final BehaviourType<FallingBlock, FallingBlock.Config> FALLING_BLOCK = registerBehaviour("falling_block", FallingBlock.class);
    public static final BehaviourType<Fire, Fire.Config> FIRE = registerBehaviour("fire", Fire.class);
    public static final BehaviourType<Flammable, Flammable.Config> FLAMMABLE = registerBehaviour("flammable", Flammable.class);
    public static final BehaviourType<Furnace, Furnace.Config> FURNACE = registerBehaviour("furnace", Furnace.class);
    public static final BehaviourType<GrassSpread, GrassSpread.Config> GRASS_SPREAD = registerBehaviour("grass_spread", GrassSpread.class);
    public static final BehaviourType<Hopper, Hopper.Config> HOPPER = registerBehaviour("hopper", Hopper.class);
    public static final BehaviourType<IgniteEntity, IgniteEntity.Config> IGNITE_ENTITY = registerBehaviour("ignite_entity", IgniteEntity.class);
    public static final BehaviourType<HorizontalFacing, HorizontalFacing.Config> HORIZONTAL_FACING = registerBehaviour("horizontal_facing", HorizontalFacing.class);
    public static final BehaviourType<Lamp, Lamp.Config> LAMP = registerBehaviour("lamp", Lamp.class);
    public static final BehaviourType<LeafDecay, LeafDecay.Config> LEAF_DECAY = registerBehaviour("leaf_decay", LeafDecay.class);
    public static final BehaviourType<Lever, Lever.Config> LEVER = registerBehaviour("lever", Lever.class);
    public static final BehaviourType<Oxidizable, Oxidizable.Config> OXIDIZABLE = registerBehaviour("oxidizable", Oxidizable.class);
    public static final BehaviourType<ParticleEmitter, ParticleEmitter.Config> PARTICLE_EMITTER = registerBehaviour("particle_emitter", ParticleEmitter.class);
    public static final BehaviourType<Powerlevel, Powerlevel.Config> POWERLEVEL = registerBehaviour("powerlevel", Powerlevel.class);
    public static final BehaviourType<Powersource, Powersource.Config> POWERSOURCE = registerBehaviour("powersource", Powersource.class);
    public static final BehaviourType<Repeater, Repeater.Config> REPEATER = registerBehaviour("repeater", Repeater.class);
    public static final BehaviourType<Rotating, Rotating.Config> ROTATING = registerBehaviour("rotating", Rotating.class);
    public static final BehaviourType<Sapling, Sapling.Config> SAPLING = registerBehaviour("sapling", Sapling.class);
    @Deprecated public static final BehaviourType<Waterloggable, Waterloggable.Config> SIMPLE_WATERLOGGABLE = registerBehaviour("simple_waterloggable", Waterloggable.class);
    public static final BehaviourType<Waterloggable, Waterloggable.Config> WATERLOGGABLE = registerBehaviour("waterloggable", Waterloggable.class);
    public static final BehaviourType<Slab, Slab.Config> SLAB = registerBehaviour("slab", Slab.class);
    public static final BehaviourType<Stairs, Stairs.Config> STAIRS = registerBehaviour("stairs", Stairs.class);

    public static final BehaviourType<StatusEffectEmitter, StatusEffectEmitter.Config> STATUS_EFFECT_EMITTER = registerBehaviour("status_effect_emitter", StatusEffectEmitter.class);
    public static final BehaviourType<Strippable, Strippable.Config> STRIPPABLE = registerBehaviour("strippable", Strippable.class);
    public static final BehaviourType<Tnt, Tnt.Config> TNT = registerBehaviour("tnt", Tnt.class);
    public static final BehaviourType<Trapdoor, Trapdoor.Config> TRAPDOOR = registerBehaviour("trapdoor", Trapdoor.class);
    public static final BehaviourType<Waxable, Waxable.Config> WAXABLE = registerBehaviour("waxable", Waxable.class);

    // Decoration
    public static final BehaviourType<Animation, Animation.Config> ANIMATION = registerBehaviour("animation", Animation.class);
    public static final BehaviourType<Bed, Bed.Config> BED = registerBehaviour("bed", Bed.class);
    public static final BehaviourType<BreakExecute, BreakExecute.Config> BREAK_EXECUTE = registerBehaviour("break_execute", BreakExecute.class);
    public static final BehaviourType<Connectable, Connectable.Config> CONNECTABLE = registerBehaviour("connectable", Connectable.class);
    public static final BehaviourType<Container, Container.Config> CONTAINER = registerBehaviour("container", Container.class);
    public static final BehaviourType<AnimatedChest, AnimatedChest.Config> ANIMATED_CHEST = registerBehaviour("animated_chest", AnimatedChest.class);
    public static final BehaviourType<Lock, Lock.Config> LOCK = registerBehaviour("lock", Lock.class); // this only exists for backwards compatibility
    public static final BehaviourType<InteractExecute, InteractExecute.Config> INTERACT_EXECUTE = registerBehaviour("interact_execute", InteractExecute.class);
    public static final BehaviourType<Seat, Seat.Config> SEAT = registerBehaviour("seat", Seat.class);
    public static final BehaviourType<Showcase, Showcase.Config> SHOWCASE = registerBehaviour("showcase", Showcase.class);
    public static final BehaviourType<Sign, Sign.Config> SIGN = registerBehaviour("sign", Sign.class);

    // Entity
    // goals
    public static final BehaviourType<FloatGoal, FloatGoal.Config> FLOAT_GOAL = registerBehaviour("float_goal", FloatGoal.class);
    public static final BehaviourType<RemoveBlockGoal, RemoveBlockGoal.Config> REMOVE_BLOCK_GOAL = registerBehaviour("remove_block_goal", RemoveBlockGoal.class);
    public static final BehaviourType<LookAtMobGoal, LookAtMobGoal.Config> LOOK_AT_MOB_GOAL = registerBehaviour("look_at_mob_goal", LookAtMobGoal.class);
    public static final BehaviourType<RandomLookAroundGoal, RandomLookAroundGoal.Config> RANDOM_LOOK_AROUND_GOAL = registerBehaviour("random_look_around_goal", RandomLookAroundGoal.class);
    public static final BehaviourType<MeleeAttackGoal, MeleeAttackGoal.Config> MELEE_ATTACK_GOAL = registerBehaviour("melee_attack_goal", MeleeAttackGoal.class);
    public static final BehaviourType<MoveThroughVillageGoal, MoveThroughVillageGoal.Config> MOVE_THROUGH_VILLAGE_GOAL = registerBehaviour("move_through_village_goal", MoveThroughVillageGoal.class);
    public static final BehaviourType<WaterAvoidingRandomStrollGoal, WaterAvoidingRandomStrollGoal.Config> WATER_AVOIDING_RANDOM_STROLL_GOAL = registerBehaviour("water_avoiding_random_stroll_goal", WaterAvoidingRandomStrollGoal.class);
    public static final BehaviourType<BreakDoorGoal, BreakDoorGoal.Config> BREAK_DOOR_GOAL = registerBehaviour("break_door_goal", BreakDoorGoal.class);
    public static final BehaviourType<BreedGoal, BreedGoal.Config> BREED_GOAL = registerBehaviour("breed_goal", BreedGoal.class);
    public static final BehaviourType<EatBlockGoal, EatBlockGoal.Config> EAT_BLOCK_GOAL = registerBehaviour("eat_block_goal", EatBlockGoal.class);
    public static final BehaviourType<FleeSunGoal, FleeSunGoal.Config> FLEE_SUN_GOAL = registerBehaviour("flee_sun_goal", FleeSunGoal.class);
    public static final BehaviourType<FollowBoatGoal, FollowBoatGoal.Config> FOLLOW_BOAT_GOAL = registerBehaviour("follow_boat_goal", FollowBoatGoal.class);
    public static final BehaviourType<FollowMobGoal, FollowMobGoal.Config> FOLLOW_MOB_GOAL = registerBehaviour("follow_mob_goal", FollowMobGoal.class);
    public static final BehaviourType<FollowParentGoal, FollowParentGoal.Config> FOLLOW_PARENT_GOAL = registerBehaviour("follow_parent_goal", FollowParentGoal.class);
    public static final BehaviourType<MoveTowardsTargetGoal, MoveTowardsTargetGoal.Config> MOVE_TOWARDS_TARGET_GOAL = registerBehaviour("move_towards_target_goal", MoveTowardsTargetGoal.class);
    public static final BehaviourType<OpenDoorGoal, OpenDoorGoal.Config> OPEN_DOOR_GOAL = registerBehaviour("open_door_goal", OpenDoorGoal.class);
    public static final BehaviourType<PanicGoal, PanicGoal.Config> PANIC_GOAL = registerBehaviour("panic_goal", PanicGoal.class);
    public static final BehaviourType<RandomStrollGoal, RandomStrollGoal.Config> RANDOM_STROLL_GOAL = registerBehaviour("random_stroll_goal", RandomStrollGoal.class);
    public static final BehaviourType<RandomSwimmingGoal, RandomSwimmingGoal.Config> RANDOM_SWIMMING_GOAL_CONFIG = registerBehaviour("random_swimming_goal", RandomSwimmingGoal.class);
    public static final BehaviourType<StrollThroughVillageGoal, StrollThroughVillageGoal.Config> STROLL_THROUGH_VILLAGE_GOAL = registerBehaviour("stroll_through_village_goal", StrollThroughVillageGoal.class);
    public static final BehaviourType<TemptGoal, TemptGoal.Config> TEMPT_GOAL = registerBehaviour("tempt_goal", TemptGoal.class);
    public static final BehaviourType<TryFindWaterGoal, TryFindWaterGoal.Config> TRY_FIND_WATER_GOAL = registerBehaviour("try_find_water_goal", TryFindWaterGoal.class);

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
