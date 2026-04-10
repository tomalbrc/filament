package de.tomalbrc.filamentweb.util;

import com.google.common.reflect.TypeToken;
import de.tomalbrc.filament.registry.FilamentComponents;
import de.tomalbrc.filament.util.annotation.Description;
import de.tomalbrc.filament.util.annotation.RegistryRef;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.SwingAnimationType;
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

    public record ConsumeEffect(@RegistryRef("consume_effect_type") Identifier type, @Nullable List<Effect> effects, @Nullable Float probability, @Nullable Float diameter) {
        public ConsumeEffect() { this(null, List.of(), null, null); }
    }
    public record Effect(@RegistryRef("consume_effect_type") Identifier id, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        public Effect() { this(null, 0,0,false, false, false); }
    }

    public record AttributeModifier(Identifier id, @RegistryRef("attribute") String type, double amount, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation, EquipmentSlot slot) {}

    public static final class ToolRule {
        @RegistryRef(value = "block", tags = true, withHash = true)
        private final String blocks = "minecraft:stone";
        private final float speed = 1;
        private final boolean correctForDrops = true;
    }

    public record KineticCondition(int maxDurationTicks, @Nullable Float minSpeed, @Nullable Float minRelativeSpeed) {
        public KineticCondition() {
            this(0, null, null);
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

    public record UseCooldownComponent(float seconds, @Nullable Identifier cooldownGroup) {
        public UseCooldownComponent() { this(0.0f, null); }
    }

    public record RepairableComponent(Identifier items) {
        public RepairableComponent() { this(Identifier.parse("minecraft:iron_ingot")); }
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
            Identifier assetId,
            @Nullable Identifier cameraOverlay,
            @RegistryRef("entity_type") List<Identifier> allowed_entities,
            boolean dispensable,
            boolean damageOnHurt,
            boolean swappable,
            boolean equip_on_interact,
            boolean can_be_sheared
    ) {
        public EquippableComponent() {
            this(EquipmentSlot.HEAD, Identifier.parse("minecraft:item.armor.equip_generic"), null, Identifier.parse("minecraft:stick"), null, true, true, true, true, true);
        }
    }

    public record JukeboxPlayableComponent(@RegistryRef("sound_event") Identifier song, boolean showInTooltip) {
        public JukeboxPlayableComponent() { this(Identifier.parse("minecraft:precipice"), true); }
    }

    public record FoodComponent(int nutrition, float saturation, boolean canAlwaysEat) {
        public FoodComponent() { this(1, 1f, false); }
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

    public record DamageResistantComponent(@RegistryRef(value = "damage_type", tagsOnly = true, withHash = true) List<String> types) {
        public DamageResistantComponent() { this(new ArrayList<>()); }
    }

    public record ItemDamage(float threshold, float base, float factor) {
        public ItemDamage() { this(0,0,0); }
    }
    public record DamageReduction(@RegistryRef(value = "damage_type", tags = true, withHash = true) String type, float base, float factor, float horizontal_blocking_angle) {
        public DamageReduction() { this(null,0f,0f,0f); }
    }

    public record BlocksAttacksComponent(float block_delay_seconds, float disable_cooldown_scale, List<DamageReduction> damageReductions, ItemDamage itemDamage, @RegistryRef("sound_event") Identifier blockSound, @RegistryRef("sound_event") Identifier disabledSound, @RegistryRef(value = "damage_type", tagsOnly = true, withHash = true) String bypassedBy) {
        public BlocksAttacksComponent() { this(1.f,1.f, List.of(), new ItemDamage(0f,0f,0f), null, null, null); }
    }

    public record BannerPatternsComponent(Identifier assetId, String translationKey) {
        public BannerPatternsComponent() { this(null, null); }
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

    public record BucketEntityDataComponent(Map<String, Object> data) {
        public BucketEntityDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record BundleContentsComponent(List<ItemStack> items) {
        public BundleContentsComponent() { this(new ArrayList<>()); }
    }

    record BlockPredicate(@RegistryRef(value = "block", tags = true, withHash = true) String blocks, Object nbt, Object state) {
        public BlockPredicate() { this(null,null,null); }
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

    public record EntityDataComponent(Map<String, Object> data) {
        public EntityDataComponent() { this(new LinkedHashMap<>()); }
    }

    public record FireworkExplosionComponent(
            net.minecraft.world.item.component.FireworkExplosion.Shape shape,
            Integer colors,
            Integer fadeColors,
            boolean hasTrail,
            boolean hasTwinkle
    ) {
        public FireworkExplosionComponent() {
            this(net.minecraft.world.item.component.FireworkExplosion.Shape.SMALL_BALL, 0, 0, false, false);
        }
    }

    public record FireworksComponent(List<FireworkExplosionComponent> explosions, int flightDuration) {
        public FireworksComponent() { this(new ArrayList<>(), 0); }
    }

    public record GliderComponent() {}

    @Description("If present, the item acts as a weapon. For attack damage see the attribute_modifiers component.")
    public record WeaponComponent(float item_damage_per_attack, float disable_blocking_for_seconds) {
        public WeaponComponent() { this(0.0f, 0.0f); }
    }

    public record InstrumentComponent(@RegistryRef("instrument") Identifier instrument) {
        public InstrumentComponent() { this(Identifier.parse("minecraft:ponder_goat_horn")); }
    }

    public record IntangibleProjectileComponent() {}

    public record LodestoneTrackerComponent(Optional<Identifier> targetDimension, Optional<String> targetPosition, boolean tracked) {
        public LodestoneTrackerComponent() { this(Optional.empty(), Optional.empty(), false); }
    }

    public record MapDecorationsComponent(@RegistryRef("map_decoration") Identifier type, double x, double z, float rotation) {
        public MapDecorationsComponent() { this(null, 0f,0f,0f); }
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

    public record SuspiciousStewEffectsComponent(List<ConsumeEffect> effects) {
        public SuspiciousStewEffectsComponent() { this(new ArrayList<>()); }
    }

    public static class ToolComponent {
        public List<ToolRule> rules = new ArrayList<>();
        public float defaultMiningSpeed = 1.0f;
        public int damagePerBlock = 1;
        public boolean can_destroy_blocks_in_creative;
    }

    public static final class TrimComponent {
        public Identifier material = Identifier.parse("minecraft:iron");
        public Identifier pattern = Identifier.parse("minecraft:iron");
    }

    public record Page(String title) {
        public Page() { this("Mc"); }
    }
    public record WritableBookContentComponent(List<Page> pages) {
        public WritableBookContentComponent() { this(new ArrayList<>()); }
    }

    public record WrittenBookContentComponent(
            String title,
            String author,
            List<Page> pages,
            Integer generation,
            boolean resolved
    ) {
        public WrittenBookContentComponent() {
            this("", "", new ArrayList<>(), 0, false);
        }
    }

    public record TextEntry(String text, String translatable, String score, String selector, String keybind, String nbt, Integer color, Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough, Boolean obfuscated, Integer shadowColor) {
        public TextEntry() { this(null, null, null, null, null, null, null, null, null, null, null, null, null); }
    }

    public static final Map<String, Type> REGISTERED_COMPONENTS = new LinkedHashMap<>();

    static {
        REGISTERED_COMPONENTS.put("filament:backpack", FilamentComponents.BackpackOptions.class);
        REGISTERED_COMPONENTS.put("filament:skin", String.class);

        REGISTERED_COMPONENTS.put("minecraft:attack_range", AttackRangeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:use_effects", UseEffectsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:minimum_attack_charge", Float.class);
        REGISTERED_COMPONENTS.put("minecraft:damage_type", DamageTypeComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:kinetic_weapon", KineticWeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:piercing_weapon", PiercingWeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:swing_animation", SwingAnimationComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:intangible_projectile", IntangibleProjectileComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:consumable", ConsumableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:jukebox_playable", JukeboxPlayableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:food", FoodComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:damage", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:max_damage", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:max_stack_size", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:unbreakable", Object.class);
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
        REGISTERED_COMPONENTS.put("minecraft:use_remainder", ItemStack.class);
        REGISTERED_COMPONENTS.put("minecraft:use_cooldown", UseCooldownComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:enchantment_glint_override", Boolean.class);
        REGISTERED_COMPONENTS.put("minecraft:tooltip_display", TooltipDisplayComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:tooltip_style", TooltipStyleComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:item_model", Identifier.class);
        REGISTERED_COMPONENTS.put("minecraft:item_name", TextEntry.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_name", CustomNameComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:lore", new TypeToken<List<TextEntry>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:rarity", RarityComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:dyed_color", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:dye", DyeColor.class);

        REGISTERED_COMPONENTS.put("minecraft:equippable", EquippableComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:glider", GliderComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:weapon", WeaponComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:instrument", InstrumentComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:note_block_sound", Identifier.class);
        REGISTERED_COMPONENTS.put("minecraft:break_sound", Identifier.class); //@RegistryRef("sound_event")
        REGISTERED_COMPONENTS.put("minecraft:tool", ToolComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:bundle_contents", BundleContentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:container", new TypeToken<List<ContainerEntry>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:container_loot", ContainerLootComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:charged_projectiles", ChargedProjectilesComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:bucket_entity_data", BucketEntityDataComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:can_break", new TypeToken<List<BlockPredicate>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:can_place_on", new TypeToken<List<BlockPredicate>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:block_state", BlockStateComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:block_entity_data", BlockEntityDataComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:entity_data", EntityDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_data", CustomDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:profile", ProfileComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:lodestone_tracker", LodestoneTrackerComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:map_color", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:map_decorations", new TypeToken<Map<String, MapDecorationsComponent>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:map_id", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:writable_book_content", WritableBookContentComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:written_book_content", WrittenBookContentComponent.class);

        REGISTERED_COMPONENTS.put("minecraft:base_color", DyeColor.class);
        REGISTERED_COMPONENTS.put("minecraft:bees", BeesComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:banner_patterns", BannerPatternsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:pot_decorations", new TypeToken<List<Identifier>>(){}.getType());
        REGISTERED_COMPONENTS.put("minecraft:potion_contents", PotionContentsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:potion_duration_scale", PotionDurationScaleComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:ominous_bottle_amplifier", Integer.class);
        REGISTERED_COMPONENTS.put("minecraft:custom_model_data", CustomModelDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:debug_stick_state", CustomDataComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:firework_explosion", FireworkExplosionComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:fireworks", FireworksComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:provides_banner_patterns", ProvidesBannerPatternsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:provides_trim_material", ProvidesTrimMaterialComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:recipes", Identifier.class);
        REGISTERED_COMPONENTS.put("minecraft:suspicious_stew_effects", SuspiciousStewEffectsComponent.class);
        REGISTERED_COMPONENTS.put("minecraft:trim", TrimComponent.class);
    }
}