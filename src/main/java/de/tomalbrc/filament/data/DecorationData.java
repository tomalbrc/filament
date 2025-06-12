package de.tomalbrc.filament.data;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.util.DecorationUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class DecorationData extends BlockData<DecorationProperties> {
    private final @Nullable List<BlockConfig> blocks;
    private final @Nullable Vector2f size;
    private final @Nullable Boolean itemFrame;
    private final @Nullable BlockState block;

    public DecorationData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable ItemResource itemResource,
            @Nullable ResourceLocation itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @Nullable Set<ResourceLocation> itemTags,
            @Nullable Set<ResourceLocation> blockTags,
            @Nullable DecorationProperties properties,
            @Nullable List<BlockConfig> blocks,
            @Nullable BlockState block,
            @Nullable Vector2f size,
            @Nullable Boolean itemFrame
    ) {
        super(id, vanillaItem, translations, itemResource, itemModel, behaviourConfig, components, itemGroup, new BlockResource(Map.of()), null, properties, false, itemTags, blockTags);
        this.blocks = blocks;
        this.size = size;
        this.itemFrame = itemFrame;
        this.block = block;
    }

    @Override
    public @NotNull ResourceProvider preferredResource() {
        assert this.itemResource != null;
        return this.itemResource;
    }

    @Override
    @NotNull
    public DecorationProperties properties() {
        if (properties == null) {
            return DecorationProperties.EMPTY;
        }
        return this.properties ;
    }

    public boolean isContainer() {
        return this.behaviour().has(Behaviours.CONTAINER);
    }

    public boolean hasBlocks() {
        return this.blocks != null;
    }

    public int countBlocks() {
        if (!this.hasBlocks())
            return 0;

        int count = 0;
        for (BlockConfig block : this.blocks) {
            count += (int) (block.size().x() * block.size().y() * block.size().z());
        }

        return count;
    }

    @Override
    public boolean requiresEntityBlock() {
        boolean singleBlock = (!this.hasBlocks() || DecorationUtil.barrierDimensions(Objects.requireNonNull(this.blocks()), 0).equals(1, 1));
        boolean hasDecorationBehaviour = this.behaviour().test((a) -> DecorationBehaviour.class.isAssignableFrom(a.type()));
        boolean canBeDyed = this.vanillaItem != null && (this.vanillaItem == Items.LEATHER_HORSE_ARMOR || this.vanillaItem == Items.FIREWORK_STAR);
        boolean groundOnly = !this.properties().placement.wall() && !this.properties().placement.ceiling();

        return !groundOnly || canBeDyed || hasDecorationBehaviour || (!singleBlock && this.size == null);
    }

    public @Nullable List<BlockConfig> blocks() {
        return this.blocks;
    }

    public @Nullable Vector2f size() {
        return this.size;
    }

    public @Nullable Boolean itemFrame() {
        return this.itemFrame;
    }

    public @NotNull BlockState block() {
        return this.block != null ? this.block : Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public Map<BlockState, BlockStateMeta> createStandardStateMap() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DecorationData) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return "DecorationData[" +
                "id=" + id + ", " +
                "itemResource=" + itemResource + ", " +
                "vanillaItem=" + vanillaItem + ", " +
                "blocks=" + blocks + ", " +
                "size=" + size + ", " +
                "properties=" + properties + ", " +
                "behaviourConfig=" + behaviour + ", " +
                "components=" + components + ", " +
                "itemGroup=" + group + ']';
    }

    public record BlockConfig(Vector3f origin,
                              Vector3f size) {
    }
}
