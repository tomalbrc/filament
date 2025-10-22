package de.tomalbrc.filament.data;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class AbstractBlockData<BlockPropertyLike extends BlockProperties> extends Data<BlockPropertyLike> {
    private final @Nullable Set<ResourceLocation> blockTags;
    private final @Nullable BlockResource blockResource;

    public AbstractBlockData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
            @Nullable BlockResource blockResource,
            @Nullable ResourceLocation itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @Nullable BlockPropertyLike properties,
            @Nullable Set<ResourceLocation> itemTags,
            @Nullable Set<ResourceLocation> blockTags
    ) {
        super(id, vanillaItem, translations, displayName, itemResource, itemModel, properties, behaviourConfig, components, itemGroup, itemTags);
        this.blockTags = blockTags;
        this.blockResource = blockResource;
    }

    public boolean requiresEntityBlock() {
        return behaviour().test((behaviourType)-> BlockBehaviourWithEntity.class.isAssignableFrom(behaviourType.type()));
    }

    @Override
    public abstract @NotNull BlockPropertyLike properties();

    // TODO: those should be part of BlockData, but needs abstract SimpleBlock first
    public abstract boolean virtual();

    public abstract Map<BlockState, BlockData.BlockStateMeta> createStandardStateMap();

    public BlockResource blockResource() {
        return this.blockResource;
    }

    public @Nullable Set<ResourceLocation> blockTags() {
        return this.blockTags;
    }

    @Override
    public String toString() {
        return "AbstractBlockData[" +
                "id=" + id + ", " +
                "vanillaItem=" + vanillaItem + ", " +
                "itemResource=" + itemResource + ", " +
                "properties=" + properties + ", " +
                "behaviourConfig=" + behaviour + ", " +
                "components=" + components + ", " +
                "itemGroup=" + group + ']';
    }

    protected BlockModelType safeBlockModelType(BlockModelType blockModelType) {
        if (PolymerBlockResourceUtils.getBlocksLeft(blockModelType) <= 0) {
            blockModelType = BlockModelType.FULL_BLOCK;
            if (PolymerBlockResourceUtils.getBlocksLeft(blockModelType) <= 0) {
                Filament.LOGGER.error("Filament: Ran out of blockModelTypes to use AND FULL_BLOCK ran out too! Using Bedrock block temporarily. Fix your Block-Config for {}!", this.id());
                return null;
            } else {
                Filament.LOGGER.error("Filament: Ran out of blockModelTypes to use! Using FULL_BLOCK for {}", this.id());
            }
        }

        return blockModelType;
    }

    protected static BlockState blockState(String str) {
        BlockStateParser.BlockResult parsed;
        try {
            parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Invalid BlockState value: " + str);
        }
        return parsed.blockState();
    }
}
