package de.tomalbrc.filament.registry;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Named;
import com.mojang.datafixers.types.templates.TypeTemplate;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.AttributeIdPrefixFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityRegistry {
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;


    public static final EntityType<BaseProjectileEntity> BASE_PROJECTILE = registerEntity("projectile", EntityType.Builder.of(BaseProjectileEntity::new, MobCategory.MISC).sized(2.f, 3.3f));

    public static final EntityType<SeatEntity> SEAT_ENTITY =  registerEntity("decoration_seat", EntityType.Builder.of(SeatEntity::new, MobCategory.MISC).noSummon());

    public static void register() {
    }

    private static <T extends Entity> EntityType<T> registerEntity(String str, EntityType.Builder<T> type) {
        var id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, str);
        var res = Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
        PolymerEntityUtils.registerType(res);

        Map<String, Type<?>> types = (Map<String, Type<?>>) DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion())).findChoiceType(References.ENTITY).types();
        types.put(id.toString(), types.get(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HUSK).toString()));

        return res;
    }

    public static ResourceKey<BlockEntityType<?>> key(ResourceLocation id) {
        return ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
    }

    public static void registerBlockEntity(ResourceKey<BlockEntityType<?>> id, BlockEntityType<?> type) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
        PolymerBlockUtils.registerBlockEntity(type);
    }
}
