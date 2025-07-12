package de.tomalbrc.filament.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Container;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.datafixer.config.DecorationDataFix;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.ComplexDecorationBlock;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;

public class DecorationRegistry {
    private static final Reference2ObjectOpenHashMap<ResourceLocation, DecorationData> decorations = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<ResourceLocation, Block> decorationBlocks = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Block, BlockEntityType<DecorationBlockEntity>> decorationBlockEntities = new Reference2ObjectOpenHashMap<>();
    public static int REGISTERED_BLOCK_ENTITIES = 0;
    public static int REGISTERED_DECORATIONS = 0;

    public static void register(InputStream inputStream) throws IOException {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(inputStream));
        DecorationData data = Json.GSON.fromJson(element, DecorationData.class);

        // backwards compatibility
        DecorationDataFix.fixup(element, data);

        Util.handleComponentsCustom(element, data);

        register(data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static public void register(DecorationData data) {
        for (Map.Entry<ResourceLocation, DecorationData> entry : decorations.entrySet()) {
            if (entry.getKey().equals(data.id())) return;
        }

        decorations.put(data.id(), data);

        BlockBehaviour.Properties blockProperties = data.properties().toBlockProperties();
        DecorationBlock block = BlockRegistry.registerBlock(BlockRegistry.key(data.id()), getBlockCreator(data), blockProperties, data.blockTags());
        decorationBlocks.put(data.id(), block);

        Item.Properties properties = data.properties().toItemProperties();
        if (data.properties().copyComponents) {
            for (TypedDataComponent component : data.vanillaItem().components()) {
                properties.component(component.type(), component.value());
            }
        }
        for (TypedDataComponent component : data.components()) {
            properties.component(component.type(), component.value());
        }

        if (data.isContainer()) {
            Container.Config container = data.behaviour().get(Behaviours.CONTAINER);
            if (container.canPickup)
                properties.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        }

        if (data.vanillaItem() == Items.LEATHER_HORSE_ARMOR || data.vanillaItem() == Items.FIREWORK_STAR) {
            properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0xdaad6d));
        }

        var item = ItemRegistry.registerItem(ItemRegistry.key(data.id()), (newProps) -> new DecorationItem(block, data, newProps), properties, data.group() != null ? data.group() : Constants.DECORATION_GROUP_ID, data.itemTags());
        BehaviourUtil.postInitItem(item, item, data.behaviour());
        BehaviourUtil.postInitBlock(item, block, block, data.behaviour());
        Translations.add(item, block, data);

        RPUtil.create(item, data);

        block.postRegister();

        FilamentRegistrationEvents.DECORATION.invoker().registered(data, item, block);
    }

    @NotNull
    private static Function<BlockBehaviour.Properties, DecorationBlock> getBlockCreator(DecorationData data) {
        Function<BlockBehaviour.Properties, DecorationBlock> gen;
        if (data.requiresEntityBlock()) {
            gen = (x) -> {
                var block = new ComplexDecorationBlock(x.pushReaction(PushReaction.BLOCK), data);

                BlockEntityType<DecorationBlockEntity> DECORATION_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DecorationBlockEntity::new, block).build();
                EntityRegistry.registerBlockEntity(EntityRegistry.key(data.id()), DECORATION_BLOCK_ENTITY);

                decorationBlockEntities.put(block, DECORATION_BLOCK_ENTITY);
                REGISTERED_BLOCK_ENTITIES++;
                REGISTERED_DECORATIONS++;

                return block;
            };
        } else {
            gen = (x) -> {
                REGISTERED_DECORATIONS++;
                return new SimpleDecorationBlock(x, data);
            };
        }
        return gen;
    }

    public static DecorationData getDecorationData(ResourceLocation resourceLocation) {
        return decorations.get(resourceLocation);
    }

    public static boolean isDecoration(BlockState blockState) {
        return blockState.getBlock() instanceof DecorationBlock;
    }

    public static DecorationBlock getDecorationBlock(ResourceLocation resourceLocation) {
        return (DecorationBlock) decorationBlocks.get(resourceLocation);
    }

    public static BlockEntityType<DecorationBlockEntity> getBlockEntityType(BlockState blockState) {
        return decorationBlockEntities.get(blockState.getBlock());
    }


    public static class DecorationDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "decorations");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/decoration", null, resourceManager, (id, inputStream) -> {
                try {
                    DecorationRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load decoration resource \"{}\".", id);
                }
            });
        }
    }
}
