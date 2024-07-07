package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.ComplexDecorationBlock;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DecorationRegistry {
    public static int REGISTERED_BLOCK_ENTITIES = 0;
    public static int REGISTERED_DECORATIONS = 0;

    private static final Object2ObjectOpenHashMap<ResourceLocation, DecorationData> decorations = new Object2ObjectOpenHashMap<>();

    private static final Object2ObjectOpenHashMap<ResourceLocation, Block> decorationBlocks = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Block, BlockEntityType<DecorationBlockEntity>> decorationBlockEntities = new Object2ObjectOpenHashMap<>();

    public static void register(InputStream inputStream) throws IOException {
        register(Json.GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), DecorationData.class));
    }

    static public void register(DecorationData data) {
        if (decorations.containsKey(data.id())) return;

        decorations.put(data.id(), data);

        DecorationBlock block;

        if (!data.isSimple()) {
            block = new ComplexDecorationBlock(FabricBlockSettings.create().nonOpaque().luminance(blockState ->
                    blockState.getValue(DecorationBlock.LIGHT_LEVEL)
            ).breakInstantly().dropsNothing().dynamicBounds().allowsSpawning((x, y, z, w) -> false).pistonBehavior(PushReaction.BLOCK), data.id());

            BlockEntityType<DecorationBlockEntity> DECORATION_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DecorationBlockEntity::new, block).build();
            EntityRegistry.registerBlockEntity(data.id(), DECORATION_BLOCK_ENTITY);

            decorationBlockEntities.put(block, DECORATION_BLOCK_ENTITY);
            REGISTERED_BLOCK_ENTITIES++;
        } else {
            block = new SimpleDecorationBlock(FabricBlockSettings.create().nonOpaque().luminance(blockState ->
                    blockState.getValue(DecorationBlock.LIGHT_LEVEL)
            ).breakInstantly().dropsNothing().dynamicBounds().allowsSpawning((x, y, z, w) -> false).pistonBehavior(data.properties() != null ? data.properties().pushReaction : PushReaction.NORMAL), data.id());
        }

        decorationBlocks.put(data.id(), block);
        BlockRegistry.registerBlock(data.id(), block);

        var properties = data.properties() != null ? data.properties().toItemProperties() : new Item.Properties().stacksTo(16);

        ItemRegistry.registerItem(data.id(), new DecorationItem(data, properties), ItemRegistry.CUSTOM_DECORATIONS);

        REGISTERED_DECORATIONS++;
    }

    public static DecorationData getDecorationDefinition(ResourceLocation resourceLocation) {
        return decorations.get(resourceLocation);
    }

    public static boolean isDecoration(Block block) {
        return decorationBlocks.containsValue(block);
    }

    public static boolean isDecoration(BlockState blockState) {
        return isDecoration(blockState.getBlock());
    }

    public static DecorationBlock getDecorationBlock(ResourceLocation resourceLocation) {
        return (DecorationBlock) decorationBlocks.get(resourceLocation);
    }

    public static BlockEntityType<DecorationBlockEntity> getBlockEntityType(BlockState blockState) {
        return decorationBlockEntities.get(blockState.getBlock());
    }


    public static class DecorationDataReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return new ResourceLocation("filament:decorations");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/decoration", path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    DecorationData data = Json.GSON.fromJson(reader, DecorationData.class);
                    DecorationRegistry.register(data);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load decoration resource \"" + entry.getKey() + "\".");
                }
            }

            Filament.LOGGER.info("filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS);
            Filament.LOGGER.info("filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES);
        }
    }
}
