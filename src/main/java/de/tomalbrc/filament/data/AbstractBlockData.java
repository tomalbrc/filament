package de.tomalbrc.filament.data;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.core.component.DataComponentMap;
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

    public AbstractBlockData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
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
    }

    public boolean requiresEntityBlock() {
        return behaviour().test((behaviourType)-> BlockBehaviourWithEntity.class.isAssignableFrom(behaviourType.type()));
    }

    @Override
    public abstract @NotNull BlockPropertyLike properties();

    // TODO: those should be part of BlockData, but needs abstract SimpleBlock first
    public abstract boolean virtual();

    public abstract Map<BlockState, BlockData.BlockStateMeta> createStandardStateMap();

    public abstract BlockResource blockResource();

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
}
