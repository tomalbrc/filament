package de.tomalbrc.filament.entity.skill.condition;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import de.tomalbrc.filament.util.Util;
import net.minecraft.resources.ResourceLocation;

public class Conditions {
    public static RuntimeTypeAdapterFactory<Condition> TYPE_ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(Condition.class, "type");

    public static ResourceLocation register(ResourceLocation id, Class<? extends Condition> type) {
        TYPE_ADAPTER_FACTORY.registerSubtype(type, id.toString());
        return id;
    }

    public static final ResourceLocation ALTITUDE =
            register(Util.id("altitude"), AltitudeCondition.class);
    public static final ResourceLocation BIOME =
            register(Util.id("biome"), BiomeCondition.class);
    public static final ResourceLocation Y_DIFF =
            register(Util.id("y_diff"), YDiffCondition.class);
    public static final ResourceLocation BLOCK_TYPE =
            register(Util.id("block_type"), BlockTypeCondition.class);
    public static final ResourceLocation IS_FILAMENT_MOB =
            register(Util.id("is_filament_mob"), IsFilamentMobCondition.class);
    public static final ResourceLocation MOUNTED =
            register(Util.id("mounted"), MountedCondition.class);
    public static final ResourceLocation IS_IN_SURVIVAL_MODE =
            register(Util.id("is_in_survival_mode"), IsInSurvivalModeCondition.class);
    public static final ResourceLocation BLOCKING =
            register(Util.id("blocking"), BlockingCondition.class);
    public static final ResourceLocation FOOD_LEVEL =
            register(Util.id("food_level"), FoodLevelCondition.class);
    public static final ResourceLocation STRING_NOT_EMPTY =
            register(Util.id("string_not_empty"), StringNotEmptyCondition.class);
    public static final ResourceLocation FOOD_SATURATION =
            register(Util.id("food_saturation"), FoodSaturationCondition.class);
    public static final ResourceLocation DISTANCE_FROM_TRACKED_LOCATION =
            register(Util.id("distance_from_tracked_location"), DistanceFromTrackedLocationCondition.class);
    public static final ResourceLocation IS_INVULNERABLE =
            register(Util.id("is_invulnerable"), IsInvulnerableCondition.class);
    public static final ResourceLocation RAINING =
            register(Util.id("raining"), RainingCondition.class);
    public static final ResourceLocation PLAYERS_ONLINE =
            register(Util.id("players_online"), PlayersOnlineCondition.class);
    public static final ResourceLocation BOUNDING_BOXES_OVERLAP =
            register(Util.id("bounding_boxes_overlap"), BoundingBoxesOverlapCondition.class);
    public static final ResourceLocation IS_PLAYER =
            register(Util.id("is_player"), IsPlayerCondition.class);
    public static final ResourceLocation VARIABLE_CONTAINS =
            register(Util.id("variable_contains"), VariableContainsCondition.class);
    public static final ResourceLocation STRING_EMPTY =
            register(Util.id("string_empty"), StringEmptyCondition.class);
    public static final ResourceLocation NIGHT =
            register(Util.id("night"), NightCondition.class);
    public static final ResourceLocation TRIGGER_BLOCK_TYPE =
            register(Util.id("trigger_block_type"), TriggerBlockTypeCondition.class);
    public static final ResourceLocation DAMAGE_TAG =
            register(Util.id("damage_tag"), DamageTagCondition.class);
    public static final ResourceLocation IS_LEASHED =
            register(Util.id("is_leashed"), IsLeashedCondition.class);
    public static final ResourceLocation TARGET_IN_LINE_OF_SIGHT =
            register(Util.id("target_in_line_of_sight"), TargetInLineOfSightCondition.class);
    public static final ResourceLocation TRIGGER_ITEM_TYPE =
            register(Util.id("trigger_item_type"), TriggerItemTypeCondition.class);
    public static final ResourceLocation HEIGHT_ABOVE =
            register(Util.id("height_above"), HeightAboveCondition.class);
    public static final ResourceLocation SUNNY =
            register(Util.id("sunny"), SunnyCondition.class);
    public static final ResourceLocation TARGET_NOT_WITHIN =
            register(Util.id("target_not_within"), TargetNotWithinCondition.class);
    public static final ResourceLocation LOOKING_AT =
            register(Util.id("looking_at"), LookingAtCondition.class);
    public static final ResourceLocation HEALTH_PERCENTAGE =
            register(Util.id("health_percentage"), HealthPercentageCondition.class);
    public static final ResourceLocation MOTION_Z =
            register(Util.id("motion_z"), MotionZCondition.class);
    public static final ResourceLocation HAS_PASSENGER =
            register(Util.id("has_passenger"), HasPassengerCondition.class);
    public static final ResourceLocation Z_DIFF =
            register(Util.id("z_diff"), ZDiffCondition.class);
    public static final ResourceLocation MOTION_Y =
            register(Util.id("motion_y"), MotionYCondition.class);
    public static final ResourceLocation HEIGHT_BELOW =
            register(Util.id("height_below"), HeightBelowCondition.class);
    public static final ResourceLocation DAY =
            register(Util.id("day"), DayCondition.class);
    public static final ResourceLocation SKILL_ON_COOLDOWN =
            register(Util.id("skill_on_cooldown"), SkillOnCooldownCondition.class);
    public static final ResourceLocation LINE_OF_SIGHT =
            register(Util.id("line_of_sight"), LineOfSightCondition.class);
    public static final ResourceLocation HOLDING =
            register(Util.id("holding"), HoldingCondition.class);
    public static final ResourceLocation ENTITY_TYPE =
            register(Util.id("entity_type"), EntityTypeCondition.class);
    public static final ResourceLocation IS_BABY =
            register(Util.id("is_baby"), IsBabyCondition.class);
    public static final ResourceLocation IS_CASTER =
            register(Util.id("is_caster"), IsCasterCondition.class);
    public static final ResourceLocation FALL_SPEED =
            register(Util.id("fall_speed"), FallSpeedCondition.class);
    public static final ResourceLocation WORLD =
            register(Util.id("world"), WorldCondition.class);
    public static final ResourceLocation HEALTH =
            register(Util.id("health"), HealthCondition.class);
    public static final ResourceLocation BIOME_TYPE_COND =
            register(Util.id("biome_type"), BiomeTypeCondition.class);
    public static final ResourceLocation LIGHT_LEVEL =
            register(Util.id("light_level"), LightLevelCondition.class);
    public static final ResourceLocation ON_GROUND =
            register(Util.id("on_ground"), OnGroundCondition.class);
    public static final ResourceLocation IS_MONSTER =
            register(Util.id("is_monster"), IsMonsterCondition.class);
    public static final ResourceLocation DUSK =
            register(Util.id("dusk"), DuskCondition.class);
    public static final ResourceLocation X_DIFF =
            register(Util.id("x_diff"), XDiffCondition.class);
    public static final ResourceLocation TARGET_WITHIN =
            register(Util.id("target_within"), TargetWithinCondition.class);
    public static final ResourceLocation IS_LIVING =
            register(Util.id("is_living"), IsLivingCondition.class);
    public static final ResourceLocation DAWN =
            register(Util.id("dawn"), DawnCondition.class);
    public static final ResourceLocation DISTANCE =
            register(Util.id("distance"), DistanceCondition.class);
    public static final ResourceLocation SPRINTING =
            register(Util.id("sprinting"), SprintingCondition.class);
    public static final ResourceLocation DIMENSION =
            register(Util.id("dimension"), DimensionCondition.class);
    public static final ResourceLocation IS_SKILL =
            register(Util.id("is_skill"), IsSkillCondition.class);
    public static final ResourceLocation INSIDE =
            register(Util.id("inside"), InsideCondition.class);
    public static final ResourceLocation DAMAGE_AMOUNT =
            register(Util.id("damage_amount"), DamageAmountCondition.class);
    public static final ResourceLocation DIRECTIONAL_VELOCITY =
            register(Util.id("directional_velocity"), DirectionalVelocityCondition.class);
    public static final ResourceLocation MOVING =
            register(Util.id("moving"), MovingCondition.class);
    public static final ResourceLocation ON_BLOCK =
            register(Util.id("on_block"), OnBlockCondition.class);
    public static final ResourceLocation TARGET_NOT_IN_LINE_OF_SIGHT =
            register(Util.id("target_not_in_line_of_sight"), TargetNotInLineOfSightCondition.class);
    public static final ResourceLocation THUNDERING =
            register(Util.id("thundering"), ThunderingCondition.class);
    public static final ResourceLocation HAS_FREE_INVENTORY_SLOT =
            register(Util.id("has_free_inventory_slot"), HasFreeInventorySlotCondition.class);
    public static final ResourceLocation METASKILL =
            register(Util.id("metaskill"), MetaskillCondition.class);
    public static final ResourceLocation GLIDING =
            register(Util.id("gliding"), GlidingCondition.class);
    public static final ResourceLocation DISTANCE_FROM_LOCATION =
            register(Util.id("distance_from_location"), DistanceFromLocationCondition.class);
    public static final ResourceLocation BURNING =
            register(Util.id("burning"), BurningCondition.class);
    public static final ResourceLocation WORLD_TIME =
            register(Util.id("world_time"), WorldTimeCondition.class);
    public static final ResourceLocation WEARING =
            register(Util.id("wearing"), WearingCondition.class);
    public static final ResourceLocation IS_CLIMBING =
            register(Util.id("is_climbing"), IsClimbingCondition.class);
    public static final ResourceLocation NAME =
            register(Util.id("name"), NameCondition.class);
    public static final ResourceLocation HEIGHT =
            register(Util.id("height"), HeightCondition.class);
    public static final ResourceLocation CHANCE =
            register(Util.id("chance"), ChanceCondition.class);
    public static final ResourceLocation BOW_TENSION =
            register(Util.id("bow_tension"), BowTensionCondition.class);
    public static final ResourceLocation VARIABLE_IS_SET =
            register(Util.id("variable_is_set"), VariableIsSetCondition.class);
    public static final ResourceLocation MOTION_X =
            register(Util.id("motion_x"), MotionXCondition.class);
    public static final ResourceLocation VEHICLE_IS_DEAD =
            register(Util.id("vehicle_is_dead"), VehicleIsDeadCondition.class);
    public static final ResourceLocation SIZE =
            register(Util.id("size"), SizeCondition.class);
    public static final ResourceLocation VARIABLE_IN_RANGE =
            register(Util.id("variable_in_range"), VariableInRangeCondition.class);
    public static final ResourceLocation ENCHANTING_EXPERIENCE =
            register(Util.id("enchanting_experience"), EnchantingExperienceCondition.class);
    public static final ResourceLocation OUTSIDE =
            register(Util.id("outside"), OutsideCondition.class);
    public static final ResourceLocation ENCHANTING_LEVEL =
            register(Util.id("enchanting_level"), EnchantingLevelCondition.class);
    public static final ResourceLocation IS_USING_SPYGLASS =
            register(Util.id("is_using_spyglass"), IsUsingSpyglassCondition.class);
    public static final ResourceLocation FIELD_OF_VIEW =
            register(Util.id("field_of_view"), FieldOfViewCondition.class);
    public static final ResourceLocation DISTANCE_FROM_SPAWN =
            register(Util.id("distance_from_spawn"), DistanceFromSpawnCondition.class);
    public static final ResourceLocation HAS_TAG =
            register(Util.id("has_tag"), HasTagCondition.class);
    public static final ResourceLocation BLOCK_TYPE_IN_RADIUS =
            register(Util.id("block_type_in_radius"), BlockTypeInRadiusCondition.class);
    public static final ResourceLocation YAW =
            register(Util.id("yaw"), YawCondition.class);
    public static final ResourceLocation HAS_ITEM =
            register(Util.id("has_item"), HasItemCondition.class);
    public static final ResourceLocation VELOCITY =
            register(Util.id("velocity"), VelocityCondition.class);
    public static final ResourceLocation HAS_OFFHAND =
            register(Util.id("has_offhand"), HasOffhandCondition.class);
    public static final ResourceLocation CROUCHING =
            register(Util.id("crouching"), CrouchingCondition.class);
    public static final ResourceLocation VARIABLE_EQUALS =
            register(Util.id("variable_equals"), VariableEqualsCondition.class);
    public static final ResourceLocation LAST_DAMAGE_CAUSE =
            register(Util.id("last_damage_cause"), LastDamageCauseCondition.class);

}
