package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.decoration.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EntityRegistry {
    public static final EntityType<BaseProjectileEntity> BASE_PROJECTILE = EntityType.Builder.of(BaseProjectileEntity::new, MobCategory.MISC).sized(2.f, 3.3f).build("base_projectile");
    public static final BlockEntityType<DecorationBlockEntity> DECORATION_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DecorationBlockEntity::new, BlockRegistry.DECORATION_BLOCK).build();

    public static final EntityType<SeatEntity> SEAT_ENTITY =  EntityType.Builder.of(SeatEntity::new, MobCategory.MISC).noSummon().noSave().build("seat");


    public static void register() {
        registerEntity(new ResourceLocation(Constants.MOD_ID, "projectile"), BASE_PROJECTILE);
        registerEntity(new ResourceLocation(Constants.MOD_ID, "decoration_seat"), SEAT_ENTITY);
        registerBlockEntity(new ResourceLocation(Constants.MOD_ID, "decoration_block_entity"), DECORATION_BLOCK_ENTITY);
    }

    private static void registerEntity(ResourceLocation id, EntityType<?> type) {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
        PolymerEntityUtils.registerType(type);
    }

    private static void registerBlockEntity(ResourceLocation id, BlockEntityType<?> type) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
        PolymerBlockUtils.registerBlockEntity(type);
    }
}
