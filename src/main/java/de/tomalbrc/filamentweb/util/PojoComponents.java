package de.tomalbrc.filamentweb.util;

import com.google.common.reflect.TypeToken;
import de.tomalbrc.filament.util.annotation.RegistryRef;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class PojoComponents {
    public static final class ContainerEntry {
        public ItemStack item = new ItemStack();
        public int slot = 0;

        public ContainerEntry() {}
    }

    public static final class ItemStack {
        @RegistryRef("item")
        public String id = "minecraft:paper";
        public int count = 1;
        public DataComponentMap components = DataComponentMap.builder().build();

        public ItemStack() {}
    }

    public record ConsumeEffect(@RegistryRef("mob_effect") Identifier id, int duration, int amplifier, boolean ambient, boolean showParticles) {}

    public record AttributeModifier(Identifier id, @RegistryRef("attribute") String type, double amount, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation, EquipmentSlot slot) {}

    public record FireworkExplosion(
            net.minecraft.world.item.component.FireworkExplosion.Shape shape,
            List<Integer> colors,
            List<Integer> fadeColors,
            boolean trail,
            boolean twinkle
    ) {
        public FireworkExplosion() {
            this(net.minecraft.world.item.component.FireworkExplosion.Shape.SMALL_BALL, new ArrayList<>(), new ArrayList<>(), false, false);
        }
    }

    public record MapDecoration(String type, int x, int z, boolean tracked) {}

    public static final class ToolRule {
        @RegistryRef(value = "block", tags = true)
        private final Identifier block = Identifier.parse("minecraft:stone");
        private final float speed= 1;
        private final boolean correctForDrops = true;
    }

    public record KineticCondition(int maxDurationTicks, Optional<Float> minSpeed, Optional<Float> minRelativeSpeed) {
        public KineticCondition() {
            this(0, Optional.empty(), Optional.empty());
        }
    }

    public record AttackRangeComponent(
            float minReach,
            float maxReach,
            float minCreativeReach,
            float maxCreativeReach,
            float hitboxMargin,
            float mobFactor
    ) {
        public AttackRangeComponent() {
            this(0.0f, 3.0f, 0.0f, 5.0f, 0.3f, 1.0f);
        }
    }

    public record FireResistantComponent() {}

    public record DeathProtectionComponent(List<ConsumeEffect> deathEffects) {
        public DeathProtectionComponent() { this(new ArrayList<>()); }
    }

    public record UseRemainderComponent(Identifier convertInto) {
        public UseRemainderComponent() { this(Identifier.parse("minecraft:air")); }
    }

    public record UseCooldownComponent(float seconds, Optional<Identifier> cooldownGroup) {
        public UseCooldownComponent() { this(0.0f, Optional.empty()); }
    }

    public record RepairableComponent(Identifier items) {
        public RepairableComponent() { this(Identifier.parse("minecraft:iron_ingot")); }
    }

    public record EnchantmentGlintOverrideComponent(boolean hasGlint) {
        public EnchantmentGlintOverrideComponent() { this(true); }
    }

    @Deprecated
    public record GlintOverrideComponent(boolean hasGlint) {
        public GlintOverrideComponent() { this(true); }
    }

    public record ItemModelComponent(Identifier model) {
        public ItemModelComponent() { this(Identifier.parse("minecraft:stick")); }
    }

    public record TooltipStyleComponent(Identifier sprite) {
        public TooltipStyleComponent() { this(Identifier.parse("minecraft:default")); }
    }

    public record TooltipDisplayComponent(boolean hideTooltip, List<Identifier> hiddenComponents) {
        public TooltipDisplayComponent() { this(false, new ArrayList<>()); }
    }

    public record UseEffectsComponent(boolean canSprint, boolean interactVibrations, float speedMultiplier) {
        public UseEffectsComponent() { this(false, true, 0.2f); }
    }

    public record MinimumAttackChargeComponent(float charge) {
        public MinimumAttackChargeComponent() { this(0.0f); }
    }

    public record DamageTypeComponent(@RegistryRef("damage_type") Identifier type) {
        public DamageTypeComponent() { this(Identifier.parse("minecraft:spear")); }
    }

    public record KineticWeaponComponent(
            int contactCooldownTicks,
            int delayTicks,
            KineticCondition dismountConditions,
            KineticCondition knockbackConditions,
            KineticCondition damageConditions,
            float forwardMovement,
            float damageMultiplier,

            @RegistryRef("sound_event")
            @Nullable Identifier sound,

            @RegistryRef("sound_event")
            @Nullable Identifier hitSound
    ) {
        public KineticWeaponComponent() {
            this(10, 0, new KineticCondition(), new KineticCondition(), new KineticCondition(), 0.0f, 1.0f, null, null);
        }
    }

    public record PiercingWeaponComponent(
            boolean dealsKnockback,
            boolean dismounts,
            @Nullable @RegistryRef("sound_event") Identifier sound,
            @Nullable @RegistryRef("sound_event") Identifier hitSound
    ) {
        public PiercingWeaponComponent() { this(true, false, null, null); }
    }

    public record SwingAnimationComponent(SwingAnimationType type, int duration) {
        public SwingAnimationComponent() { this(SwingAnimationType.WHACK, 6); }
    }

    public record EquippableComponent(
            EquipmentSlot slot,
            @RegistryRef("sound_event") Identifier equipSound,
            Identifier model,
            @Nullable Identifier cameraOverlay,
            boolean dispensable,
            boolean damageOnHurt,
            boolean swappable
    ) {
        public EquippableComponent() {
            this(EquipmentSlot.HEAD, Identifier.parse("minecraft:item.armor.equip_generic"), Identifier.parse("minecraft:stick"), null, true, true, true);
        }
    }

    public record JukeboxPlayableComponent(Identifier song, boolean showInTooltip) {
        public JukeboxPlayableComponent() { this(Identifier.parse("minecraft:precipice"), true); }
    }

    public record FoodComponent(int nutrition, float saturationModifier, boolean canAlwaysEat) {
        public FoodComponent() { this(0, 0.0f, false); }
    }

    public record ConsumableComponent(
            float consumeSeconds,
            ItemUseAnimation animation,
            @RegistryRef("sound_event") Identifier sound,
            boolean hasConsumeParticles,
            List<Map<String, Object>> onConsumeEffects
    ) {
        public ConsumableComponent() {
            this(1.6f, ItemUseAnimation.EAT, Identifier.parse("minecraft:entity.generic.eat"), true, new ArrayList<>());
        }
    }

    public record DamageComponent(int damage) {
        public DamageComponent() { this(0); }
    }

    public record MaxDamageComponent(int maxDamage) {
        public MaxDamageComponent() { this(0); }
    }

    public record MaxStackSizeComponent(int size) {
        public MaxStackSizeComponent() { this(64); }
    }

    public record UnbreakableComponent(boolean value) {
        public UnbreakableComponent() { this(true); }
    }

    public record RepairCostComponent(int cost) {
        public RepairCostComponent() { this(0); }
    }

    public record EnchantableComponent(int value) {
        public EnchantableComponent() { this(0); }
    }

    public record EnchantmentsComponent(Map<Identifier, Integer> enchantments) {
        public EnchantmentsComponent() { this(new LinkedHashMap<>()); }
    }

    public record StoredEnchantmentsComponent(Map<Identifier, Integer> enchantments) {
        public StoredEnchantmentsComponent() { this(new LinkedHashMap<>()); }
    }

    public record DamageResistantComponent(@RegistryRef(value = "damage_type", tagsOnly = true) List<Identifier> types) {
        public DamageResistantComponent() { this(new ArrayList<>()); }
    }

    public record BlocksAttacksComponent(List<Identifier> bypassedBy) {
        public BlocksAttacksComponent() { this(new ArrayList<>()); }
    }

    public record BannerPatternsComponent(Identifier assetId, String translationKey) {
        public BannerPatternsComponent() { this(null, null); }
    }

    public record BaseColorComponent(String color) {
        public BaseColorComponent() { this("white"); }
    }

    public record BeesComponent(List<Map<String, Object>> bees) {
        public BeesComponent() { this(new ArrayList<>()); }
    }

    public record BlockEntityDataComponent(Map<String, Object> data) {
        public BlockEntityDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record BlockStateComponent(Map<String, Object> states) {
        public BlockStateComponent() { this(new LinkedHashMap<>()); }
    }

    public record BreakSoundComponent(@RegistryRef("sound_event") Identifier sound) {
        public BreakSoundComponent() { this(Identifier.parse("minecraft:block.stone.break")); }
    }

    public record BucketEntityDataComponent(Map<String, Object> data) {
        public BucketEntityDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record BundleContentsComponent(List<ItemStack> items) {
        public BundleContentsComponent() { this(new ArrayList<>()); }
    }

    public record CanBreakComponent(List<Identifier> blocks) {
        public CanBreakComponent() { this(new ArrayList<>()); }
    }

    public record CanPlaceOnComponent(List<Identifier> blocks) {
        public CanPlaceOnComponent() { this(new ArrayList<>()); }
    }

    public record ChargedProjectilesComponent(List<ItemStack> items) {
        public ChargedProjectilesComponent() { this(new ArrayList<>()); }
    }

    public record ContainerLootComponent(@RegistryRef("loot_table") Identifier lootTable, long seed) {
        public ContainerLootComponent() { this(Identifier.parse("minecraft:empty"), 0L); }
    }

    public record CustomDataComponent(Map<String, Object> data) {
        public CustomDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record CustomModelDataComponent(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
        public CustomModelDataComponent() { this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); }
    }

    public record CustomNameComponent(Object jsonText) {
        public CustomNameComponent() { this(null); }
    }

    public record DyeComponent(DyeColor color) {
        public DyeComponent() { this(DyeColor.WHITE); }
    }

    public record DyedColorComponent(int rgb) {
        public DyedColorComponent() { this(0xFFFFFF); }
    }

    public record EntityDataComponent(Map<String, Object> data) {
        public EntityDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record FireworkExplosionComponent(
            net.minecraft.world.item.component.FireworkExplosion.Shape shape,
            List<Integer> colors,
            List<Integer> fadeColors,
            boolean trail,
            boolean twinkle
    ) {
        public FireworkExplosionComponent() {
            this(net.minecraft.world.item.component.FireworkExplosion.Shape.SMALL_BALL, new ArrayList<>(), new ArrayList<>(), false, false);
        }
    }

    public record FireworksComponent(List<FireworkExplosionComponent> explosions, int flightDuration) {
        public FireworksComponent() { this(new ArrayList<>(), 0); }
    }

    public record GliderComponent() {}

    public record WeaponComponent(float attackDamage, float attackSpeed) {
        public WeaponComponent() { this(0.0f, 0.0f); }
    }

    public record InstrumentComponent(@RegistryRef("instrument") Identifier instrument) {
        public InstrumentComponent() { this(Identifier.parse("minecraft:ponder_goat_horn")); }
    }

    public record IntangibleProjectileComponent() {}

    public record ItemNameComponent(String text) {
        public ItemNameComponent() { this(""); }
    }

    public record LoreComponent(List<String> lines) {
        public LoreComponent() { this(new ArrayList<>()); }
    }

    public record LodestoneTrackerComponent(Optional<Identifier> targetDimension, Optional<String> targetPosition, boolean tracked) {
        public LodestoneTrackerComponent() { this(Optional.empty(), Optional.empty(), false); }
    }

    public record MapColorComponent(int color) {
        public MapColorComponent() { this(0); }
    }

    public record MapDecorationComponent(String id, int x, int z, boolean tracked) {
        public MapDecorationComponent() { this("", 0, 0, false); }
    }

    public record MapDecorationsComponent(List<MapDecorationComponent> decorations) {
        public MapDecorationsComponent() { this(new ArrayList<>()); }
    }

    public record MapIdComponent(int id) {
        public MapIdComponent() { this(0); }
    }

    public record NoteBlockSoundComponent(Identifier sound) {
        public NoteBlockSoundComponent() { this(Identifier.parse("minecraft:block.note_block.harp")); }
    }

    public record OminousBottleAmplifierComponent(int value) {
        public OminousBottleAmplifierComponent() { this(0); }
    }

    public record PotDecorationsComponent(List<Identifier> patterns) {
        public PotDecorationsComponent() { this(new ArrayList<>()); }
    }

    public record PotionContentsComponent(List<Identifier> effects) {
        public PotionContentsComponent() { this(new ArrayList<>()); }
    }

    public record PotionDurationScaleComponent(float scale) {
        public PotionDurationScaleComponent() { this(1.0f); }
    }

    public record ProfileComponent(UUID uuid, String name, Map<String, String> properties) {
        public ProfileComponent() { this(null, "", new LinkedHashMap<>()); }
    }

    public record ProvidesBannerPatternsComponent(List<Identifier> patterns) {
        public ProvidesBannerPatternsComponent() { this(new ArrayList<>()); }
    }

    public record ProvidesTrimMaterialComponent(Identifier material) {
        public ProvidesTrimMaterialComponent() { this(Identifier.parse("minecraft:iron")); }
    }

    public record RarityComponent(String rarity) {
        public RarityComponent() { this("common"); }
    }

    public record RecipesComponent(List<Identifier> recipes) {
        public RecipesComponent() { this(new ArrayList<>()); }
    }

    public record SuspiciousStewEffectsComponent(List<ConsumeEffect> effects) {
        public SuspiciousStewEffectsComponent() { this(new ArrayList<>()); }
    }

    public static final class ToolComponent {
        public List<ToolRule> rules = new ArrayList<>();
        public float defaultMiningSpeed = 1.0f;
        public int damagePerBlock = 1;
    }

    public static final class TrimComponent {
        private Identifier material = Identifier.parse("minecraft:iron");
        private Identifier pattern = Identifier.parse("minecraft:iron");
    }

    public record WritableBookContentComponent(List<String> pages) {
        public WritableBookContentComponent() { this(new ArrayList<>()); }
    }

    public record WrittenBookContentComponent(
            String title,
            String author,
            List<String> pages,
            boolean resolved
    ) {
        public WrittenBookContentComponent() {
            this("", "", new ArrayList<>(), false);
        }
    }

    public static final Map<String, Type> REGISTERED_COMPONENTS = new LinkedHashMap<>();

    static {
        REGISTERED_COMPONENTS.put("minecraft:attack_range", AttackRangeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:use_effects", UseEffectsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:minimum_attack_charge", MinimumAttackChargeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:damage_type", DamageTypeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:kinetic_weapon", KineticWeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:piercing_weapon", PiercingWeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:swing_animation", SwingAnimationComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:intangible_projectile", IntangibleProjectileComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:consumable", ConsumableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:jukebox_playable", JukeboxPlayableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:food", FoodComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:damage", DamageComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:max_damage", MaxDamageComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:max_stack_size", MaxStackSizeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:unbreakable", UnbreakableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:repair_cost", RepairCostComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:enchantable", EnchantableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:enchantments", EnchantmentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:stored_enchantments", StoredEnchantmentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:attribute_modifiers", new TypeToken<List<AttributeModifier>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:damage_resistant", DamageResistantComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:blocks_attacks", BlocksAttacksComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:repairable", RepairableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:death_protection", DeathProtectionComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:fire_resistant", FireResistantComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:use_remainder", UseRemainderComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:use_cooldown", UseCooldownComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:enchantment_glint_override", EnchantmentGlintOverrideComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:glint_override", GlintOverrideComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:tooltip_display", TooltipDisplayComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:tooltip_style", TooltipStyleComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:item_model", ItemModelComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:item_name", ItemNameComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_name", CustomNameComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:lore", LoreComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:rarity", RarityComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:dyed_color", DyedColorComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:dye", DyeComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:equippable", EquippableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:glider", GliderComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:weapon", WeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:instrument", InstrumentComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:note_block_sound", NoteBlockSoundComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:break_sound", BreakSoundComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:tool", ToolComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:bundle_contents", BundleContentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:container", new TypeToken<List<ContainerEntry>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:container_loot", ContainerLootComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:charged_projectiles", ChargedProjectilesComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:bucket_entity_data", BucketEntityDataComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:can_break", CanBreakComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:can_place_on", CanPlaceOnComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:block_state", BlockStateComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:block_entity_data", BlockEntityDataComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:entity_data", EntityDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_data", CustomDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:profile", ProfileComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:lodestone_tracker", LodestoneTrackerComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:map_color", MapColorComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:map_decorations", MapDecorationsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:map_id", MapIdComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:writable_book_content", WritableBookContentComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:written_book_content", WrittenBookContentComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:base_color", DyeColor.class);
        REGISTERED_COMPONENTS.put("minecraft:bees", BeesComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:banner_patterns", BannerPatternsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:pot_decorations", PotDecorationsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:potion_contents", PotionContentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:potion_duration_scale", PotionDurationScaleComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:ominous_bottle_amplifier", OminousBottleAmplifierComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_model_data", CustomModelDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:debug_stick_state", CustomDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:firework_explosion", FireworkExplosionComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:fireworks", FireworksComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:provides_banner_patterns", ProvidesBannerPatternsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:provides_trim_material", ProvidesTrimMaterialComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:recipes", RecipesComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:suspicious_stew_effects", SuspiciousStewEffectsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:trim", TrimComponent.class);
    }
}