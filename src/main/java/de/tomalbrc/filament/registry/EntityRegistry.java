package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EntityRegistry {
    public static final EntityType<BaseProjectileEntity> BASE_PROJECTILE = registerEntity("projectile", EntityType.Builder.of(BaseProjectileEntity::new, MobCategory.MISC).sized(2.f, 3.3f));

    public static final EntityType<SeatEntity> SEAT_ENTITY =  registerEntity("decoration_seat", EntityType.Builder.of(SeatEntity::new, MobCategory.MISC).noSummon());

    public static void register() {
    }

    private static <T extends Entity> EntityType<T> registerEntity(String str, EntityType.Builder<T> type) {
        var id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, str);
        var res = Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
        PolymerEntityUtils.registerType(res);
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
