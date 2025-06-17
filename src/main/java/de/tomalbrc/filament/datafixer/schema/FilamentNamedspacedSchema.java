package de.tomalbrc.filament.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

public class FilamentNamedspacedSchema extends NamespacedSchema {
	public FilamentNamedspacedSchema(int versionKey, Schema parent) {
		super(versionKey, parent);
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
		super.registerTypes(schema, entityTypes, blockEntityTypes);
		schema.registerType(false, References.CHUNK, () -> DSL.optionalFields("block_entities", DSL.remainder(), "sections", DSL.remainder()));
	}

	@Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(final Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
		schema.registerSimple(map, "mynamespace:benchy");
		schema.registerSimple(map, "mynamespace:small_gold_coin_piles");
		return map;
    }
}