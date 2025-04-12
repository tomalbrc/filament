package de.tomalbrc.filament.registry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.entity.EntityData;
import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.item.TridentEntity;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Translations;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EntityRegistry {
    public static Map<ResourceLocation, Collection<ResourceLocation>> ENTITY_TAGS = new Object2ReferenceOpenHashMap<>();

    private static final Reference2ObjectOpenHashMap<ResourceLocation, EntityData> types = new Reference2ObjectOpenHashMap<>();

    public static final EntityType<BaseProjectileEntity> BASE_PROJECTILE = registerEntity("projectile", EntityType.Builder.of(BaseProjectileEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).noSummon());
    public static final EntityType<TridentEntity> FILAMENT_TRIDENT = registerEntity("trident", EntityType.Builder.of(TridentEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).eyeHeight(0.13f).noSummon());
    public static final EntityType<SeatEntity> SEAT_ENTITY =  registerEntity("decoration_seat", EntityType.Builder.of(SeatEntity::new, MobCategory.MISC).noSummon().updateInterval(20));

    public static void register(InputStream inputStream) throws IOException {
        var element = JsonParser.parseReader(new InputStreamReader(inputStream));
        EntityData data = Json.GSON.fromJson(element, EntityData.class);
        register(data);
    }

    static public void register(EntityData data) {
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(data.id())) return;
        types.put(data.id(), data);

        var size = (data.properties().size == null || Objects.requireNonNull(data.properties().size).size() < 2) ? ImmutableList.of(1.f, 1.f) : data.properties().size;
        assert size != null;

        var builder = EntityType.Builder.of(FilamentMob::new, data.properties().category).sized(size.getFirst(), size.getLast());
        if (data.properties().fireImmune)
            builder.fireImmune();
        if (data.properties().noSave)
            builder.noSave();
        if (data.properties().noSummon)
            builder.noSummon();

        AttributeSupplier.Builder attributeBuilder = Mob.createMobAttributes();
        var attrMap = data.attributes();
        if (attrMap != null) {
            for (Map.Entry<ResourceLocation, Double> entry : attrMap.entrySet()) {
                var attr = BuiltInRegistries.ATTRIBUTE.get(entry.getKey());
                if (attr.isEmpty()) {
                    Filament.LOGGER.error("Invalid attribute id {} for entity {}", entry.getKey(), data.id());
                    continue;
                }
                attributeBuilder.add(attr.orElseThrow(), entry.getValue());
            }
        }

        var type = registerEntity(data.id(), builder);
        FabricDefaultAttributeRegistry.register(type, attributeBuilder.build());

        Translations.add(type, data);

        ENTITY_TAGS.put(data.id(), data.entityTags());

        FilamentRegistrationEvents.ENTITY.invoker().registered(data, type);
    }

    public static EntityData getData(ResourceLocation resourceLocation) {
        return types.get(resourceLocation);
    }

    public static void register() {
    }

    private static <T extends Entity> EntityType registerEntity(String str, EntityType.Builder type) {
        var id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, str);
        return registerEntity(id, type);
    }

    private static <T extends Entity> EntityType registerEntity(ResourceLocation id, EntityType.Builder type) {
        Map<String, Type<?>> types = (Map<String, Type<?>>) DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion())).findChoiceType(References.ENTITY).types();
        types.put(id.toString(), types.get(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE).toString()));

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


    public static class EntityDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "entities");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/entity", null, resourceManager, (id, inputStream) -> {
                try {
                    EntityRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load entity resource \"{}\".", id);
                }
            });
        }
    }
}
