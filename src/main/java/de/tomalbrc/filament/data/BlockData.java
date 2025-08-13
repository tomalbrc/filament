package de.tomalbrc.filament.data;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.util.FilamentBlockResourceUtils;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class BlockData extends AbstractBlockData<BlockProperties> {
    private final @Nullable BlockStateMappedProperty<BlockModelType> blockModelType;

    public BlockData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
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
        super(id, vanillaItem, translations, displayName, itemResource, blockResource, itemModel, behaviourConfig, components, itemGroup, properties, itemTags, blockTags);
        this.blockModelType = blockModelType;
    }

    @Override
    @NotNull
    public BlockProperties properties() {
        if (properties == null) {
            properties = new BlockProperties();
        }
        return properties;
    }

    @Override
    public boolean virtual() {
        return this.properties().virtual;
    }

    @Override
    public Map<BlockState, BlockStateMeta> createStandardStateMap() {
        Reference2ReferenceArrayMap<BlockState, BlockStateMeta> val = new Reference2ReferenceArrayMap<>();

        if (blockResource() != null && blockResource().models() != null && this.blockModelType != null) {
            for (Map.Entry<String, PolymerBlockModel> entry : this.blockResource().models().entrySet()) {
                if (entry.getKey().equals("default")) {
                    var type = safeBlockModelType(this.blockModelType.getRawValue());
                    BlockState requestedState = type == null ? null : FilamentBlockResourceUtils.requestBlock(type, entry.getValue(), this.virtual());
                    val.put(BuiltInRegistries.BLOCK.getValue(id).defaultBlockState(), BlockStateMeta.of(type == null ? Blocks.BEDROCK.defaultBlockState() : requestedState, entry.getValue()));
                } else {
                    BlockState state = blockState(String.format("%s[%s]", id, entry.getKey()));
                    BlockModelType type;
                    if (this.blockModelType.isMap()) {
                        type = safeBlockModelType(this.blockModelType.getOrDefault(state, BlockModelType.FULL_BLOCK));
                    } else {
                        type = safeBlockModelType(this.blockModelType.getRawValue());
                    }

                    BlockState requestedState = type == null ? null : FilamentBlockResourceUtils.requestBlock(type, entry.getValue(), this.virtual());
                    val.put(state, BlockStateMeta.of(type == null ? Blocks.BEDROCK.defaultBlockState() : requestedState, entry.getValue()));
                }
            }
        }

        return val;
    }

    @Override
    public @NotNull ResourceProvider preferredResource() {
        return blockResource();
    }

    public @Nullable BlockStateMappedProperty<BlockModelType> blockModelType() {
        return blockModelType;
    }

    @Override
    public String toString() {
        return "BlockData[" +
                "id=" + id + ", " +
                "vanillaItem=" + vanillaItem + ", " +
                "blockResource=" + blockResource() + ", " +
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
