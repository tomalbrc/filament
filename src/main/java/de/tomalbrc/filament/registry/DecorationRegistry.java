package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.block.ComplexDecorationBlock;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.apache.commons.io.FileUtils;
import de.tomalbrc.resin.data.AjLoader;
import de.tomalbrc.resin.model.AjModel;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;

public class DecorationRegistry {
    public static int REGISTERED_BLOCK_ENTITIES = 0;
    public static int REGISTERED_DECORATIONS = 0;

    public static final File DIR = Constants.CONFIG_DIR.resolve("decoration").toFile();

    private static final Object2ObjectOpenHashMap<ResourceLocation, DecorationData> decorations = new Object2ObjectOpenHashMap<>();

    private static final Object2ObjectOpenHashMap<ResourceLocation, Block> decorationBlocks = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Block, BlockEntityType<DecorationBlockEntity>> decorationBlockEntities = new Object2ObjectOpenHashMap<>();

    private static final Object2ObjectOpenHashMap<String, AjModel> ajmodels = new Object2ObjectOpenHashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        if (!DIR.exists() || !DIR.isDirectory()) {
            DIR.mkdirs();
            return;
        }

        preloadModels();

        Collection<File> files = FileUtils.listFiles(DIR, new String[]{"json"}, true);
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    DecorationData data = Json.GSON.fromJson(reader, DecorationData.class);
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
                        ).breakInstantly().dropsNothing().dynamicBounds().allowsSpawning((x, y, z, w) -> false).pistonBehavior(PushReaction.NORMAL), data.id());
                    }

                    decorationBlocks.put(data.id(), block);
                    BlockRegistry.registerBlock(data.id(), block);
                    ItemRegistry.registerItem(data.id(), new DecorationItem(data), ItemRegistry.CUSTOM_DECORATIONS);

                    REGISTERED_DECORATIONS++;
                } catch (Throwable throwable) {
                    Filament.LOGGER.error("Error reading decoration JSON file: {}", file.getAbsolutePath(), throwable);
                }
            }
        }
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

    public static AjModel getModel(String name) {
        return ajmodels.get(name);
    }

    private static void preloadModels() {
        String path = String.format("%s/ajmodel/", Constants.CONFIG_DIR);
        File modelDir = new File(path);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            modelDir.mkdirs();
            return;
        }

        Collection<File> files = FileUtils.listFiles(modelDir, new String[]{"json"}, true);

        if (files != null) {
            for (File file : files) {
                AjModel model = AjLoader.require(file.getPath());
                ajmodels.put(file.getName(), model);
            }
        }
    }
}
