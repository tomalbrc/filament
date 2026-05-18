package de.tomalbrc.filament.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record StationDef(
        Identifier id,
        MenuType<?> menuType,
        List<SlotDef> slots,
        Optional<Grid> grid,
        Optional<ItemStackTemplate> backgroundItem,
        Optional<Map<Integer, DecorationDef>> decorations,
        int processingTime,
        boolean persistent
) {
    public static final Codec<StationDef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(StationDef::id),
                    BuiltInRegistries.MENU.byNameCodec().fieldOf("menu_type").forGetter(StationDef::menuType),
                    SlotDef.CODEC.listOf().fieldOf("slots").forGetter(StationDef::slots),
                    Grid.CODEC.optionalFieldOf("grid").forGetter(StationDef::grid),
                    ItemStackTemplate.CODEC.optionalFieldOf("background_item").forGetter(StationDef::backgroundItem),
                    Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf), DecorationDef.CODEC).optionalFieldOf("background_item").forGetter(StationDef::decorations),
                    Codec.INT.optionalFieldOf("processing_time", 0).forGetter(StationDef::processingTime),
                    Codec.BOOL.optionalFieldOf("persistent", false).forGetter(StationDef::persistent)
            ).apply(instance, StationDef::new)
    );

    public ItemStack backgroundItemStack() {
        return backgroundItem.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public Optional<SlotRole> getSlotRole(int index) {
        return slots.stream()
                .filter(s -> s.slotIndex() == index)
                .findFirst()
                .map(SlotDef::role);
    }

    public Optional<Integer> getSlotIndex(String name) {
        return slots.stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .map(SlotDef::slotIndex);
    }

    public Optional<String> getSlotName(int index) {
        return slots.stream()
                .filter(s -> s.slotIndex() == index)
                .map(SlotDef::name)
                .findFirst();
    }

    public Optional<String> getSlotNameAt(int row, int col) {
        return slots.stream()
                .filter(s -> s.row().isPresent() && s.col().isPresent()
                        && s.row().get() == row && s.col().get() == col)
                .map(SlotDef::name)
                .findFirst();
    }

    public Optional<SlotRole> getSlotRoleByName(String name) {
        return slots.stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .map(SlotDef::role);
    }

    public Optional<String> getOutputSlotName() {
        return slots.stream()
                .filter(s -> s.role() == SlotRole.OUTPUT)
                .findFirst()
                .map(SlotDef::name);
    }

    public Optional<Integer> getSlotIndexAt(int row, int col) {
        return slots.stream()
                .filter(s -> s.row().isPresent() && s.col().isPresent()
                        && s.row().get() == row && s.col().get() == col)
                .map(StationDef.SlotDef::slotIndex)
                .findFirst();
    }

    public record DecorationDef(ItemStackTemplate item, Optional<String> command) {
        public static final Codec<DecorationDef> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ItemStackTemplate.CODEC.fieldOf("name").forGetter(DecorationDef::item),
                        Codec.STRING.optionalFieldOf("name").forGetter(DecorationDef::command)
                ).apply(inst, DecorationDef::new)
        );
    }

    public record SlotDef(String name, int slotIndex, SlotRole role, Optional<Integer> row, Optional<Integer> col) {
        public static final Codec<SlotDef> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("name").forGetter(SlotDef::name),
                        Codec.INT.fieldOf("slot_index").forGetter(SlotDef::slotIndex),
                        SlotRole.CODEC.fieldOf("role").forGetter(SlotDef::role),
                        Codec.INT.optionalFieldOf("row").forGetter(SlotDef::row),
                        Codec.INT.optionalFieldOf("col").forGetter(SlotDef::col)
                ).apply(inst, SlotDef::new)
        );
    }

    public enum SlotRole { INPUT, OUTPUT, FUEL;
        public static final Codec<SlotRole> CODEC = Codec.STRING.xmap(x -> SlotRole.valueOf(x.toUpperCase(Locale.ROOT)), x -> x.name().toLowerCase(Locale.ROOT));
    }

    public record Grid(int rows, int columns) {
        public static final Codec<Grid> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("rows").forGetter(Grid::rows),
                        Codec.INT.fieldOf("columns").forGetter(Grid::columns)
                ).apply(instance, Grid::new)
        );
    }
}