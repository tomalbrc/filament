package de.tomalbrc.filament.data;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.FilamentBlockResourceUtils;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class BlockData extends Data {
    private final @NotNull BlockResource blockResource;
    private final @Nullable BlockStateMappedProperty<BlockModelType> blockModelType;
    private final @Nullable BlockProperties properties;
    private final @Nullable Set<ResourceLocation> blockTags;

    public BlockData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable ItemResource itemResource,
            @Nullable ResourceLocation itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @NotNull BlockResource blockResource,
            @Nullable BlockStateMappedProperty<BlockModelType> blockModelType,
            @Nullable BlockProperties properties,
            @Nullable Set<ResourceLocation> itemTags,
            @Nullable Set<ResourceLocation> blockTags
    ) {
        super(id, vanillaItem, translations, itemResource, itemModel, behaviourConfig, components, itemGroup, itemTags);
        this.blockResource = blockResource;
        this.blockModelType = blockModelType;
        this.properties = properties;
        this.blockTags = blockTags;
    }

    @NotNull
    public BlockProperties properties() {
        if (properties == null) {
            return BlockProperties.EMPTY;
        }
        return properties;
    }

    public Map<BlockState, BlockStateMeta> createStandardStateMap() {
        Reference2ReferenceArrayMap<BlockState, BlockStateMeta> val = new Reference2ReferenceArrayMap<>();

        if (blockResource.models() != null && this.blockModelType != null) {
            for (Map.Entry<String, PolymerBlockModel> entry : this.blockResource.models().entrySet()) {
                if (entry.getKey().equals("default")) {
                    var type = safeBlockModelType(this.blockModelType.getRawValue());
                    BlockState requestedState = type == null ? null : FilamentBlockResourceUtils.requestBlock(type, entry.getValue());
                    val.put(BuiltInRegistries.BLOCK.getValue(id).defaultBlockState(), BlockStateMeta.of(type == null ? Blocks.BEDROCK.defaultBlockState() : requestedState, entry.getValue()));
                } else {
                    BlockState state = blockState(String.format("%s[%s]", id, entry.getKey()));
                    BlockModelType type;
                    if (this.blockModelType.isMap()) {
                        type = safeBlockModelType(this.blockModelType.getOrDefault(state, BlockModelType.FULL_BLOCK));
                    } else {
                        type = safeBlockModelType(this.blockModelType.getRawValue());
                    }

                    BlockState requestedState = type == null ? null : FilamentBlockResourceUtils.requestBlock(type, entry.getValue());
                    val.put(state, BlockStateMeta.of(type == null ? Blocks.BEDROCK.defaultBlockState() : requestedState, entry.getValue()));
                }
            }
        }

        return val;
    }

    private static BlockState blockState(String str) {
        BlockStateParser.BlockResult parsed;
        try {
            parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Invalid BlockState value: " + str);
        }
        return parsed.blockState();
    }

    private BlockModelType safeBlockModelType(BlockModelType blockModelType) {
        if (PolymerBlockResourceUtils.getBlocksLeft(blockModelType) <= 0) {
            blockModelType = BlockModelType.FULL_BLOCK;
            if (PolymerBlockResourceUtils.getBlocksLeft(blockModelType) <= 0) {
                Filament.LOGGER.error("Filament: Ran out of blockModelTypes to use AND FULL_BLOCK ran out too! Using Bedrock block temporarily. Fix your Block-Config for {}!", this.id());
                return null;
            } else {
                Filament.LOGGER.error("Filament: Ran out of blockModelTypes to use! Using FULL_BLOCK");
            }
        }

        return blockModelType;
    }

    public @NotNull BlockResource blockResource() {
        return blockResource;
    }

    public @Nullable BlockStateMappedProperty<BlockModelType> blockModelType() {
        return blockModelType;
    }

    public @Nullable Set<ResourceLocation> blockTags() {
        return this.blockTags;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockData) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BlockData[" +
                "id=" + id + ", " +
                "vanillaItem=" + vanillaItem + ", " +
                "blockResource=" + blockResource + ", " +
                "itemResource=" + itemResource + ", " +
                "blockModelType=" + blockModelType + ", " +
                "properties=" + properties + ", " +
                "behaviourConfig=" + behaviour + ", " +
                "components=" + components + ", " +
                "itemGroup=" + group + ']';
    }


    public record BlockStateMeta(BlockState blockState, PolymerBlockModel polymerBlockModel) {
        public static BlockStateMeta of(BlockState blockState, PolymerBlockModel blockModel) {
            return new BlockStateMeta(blockState, blockModel);
        }
    }
}
