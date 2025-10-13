package de.tomalbrc.filament.data;

import de.tomalbrc.filament.api.behaviour.*;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class DecorationData extends AbstractBlockData<DecorationProperties> {
    private final @Nullable List<BlockConfig> blocks;
    private final @Nullable Vector2f size;
    private final @Nullable Boolean itemFrame;
    private final @Nullable BlockStateMappedProperty<BlockState> block;

    public DecorationData(
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
            @Nullable Set<ResourceLocation> itemTags,
            @Nullable Set<ResourceLocation> blockTags,
            @Nullable DecorationProperties properties,
            @Nullable List<BlockConfig> blocks,
            @Nullable BlockStateMappedProperty<BlockState> block,
            @Nullable Vector2f size,
            @Nullable Boolean itemFrame
            ) {
        super(id, vanillaItem, translations, displayName, itemResource, blockResource, itemModel, behaviourConfig, components, itemGroup, properties, itemTags, blockTags);
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
            this.properties = new DecorationProperties();
        }
        return this.properties ;
    }

    public boolean isContainer() {
        return behaviour().test((behaviourType)-> ContainerLike.class.isAssignableFrom(behaviourType.type()));
    }

    public static ContainerLike getFirstContainer(BehaviourHolder holder) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : holder.getBehaviours()) {
            if (behaviour.getValue() instanceof ContainerLike containerLike) {
                return containerLike;
            }
        }
        return null;
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

    public @Nullable BlockState block(BlockState blockState) {
        return this.block != null ? this.block.getOrDefault(blockState, null) : null;
    }

    @Override
    public Map<BlockState, BlockData.BlockStateMeta> createStandardStateMap() {
        Reference2ReferenceArrayMap<BlockState, BlockData.BlockStateMeta> val = new Reference2ReferenceArrayMap<>();

        if (blockResource() != null && blockResource().models() != null) {
            for (Map.Entry<String, PolymerBlockModel> entry : this.blockResource().models().entrySet()) {
                if (entry.getKey().equals("default")) {
                    var customState = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
                    val.put(customState, BlockData.BlockStateMeta.of(null, entry.getValue()));
                } else {
                    BlockState state = blockState(String.format("%s[%s]", id, entry.getKey()));

                    if (state.hasProperty(BlockStateProperties.WATERLOGGED) && !entry.getKey().contains(BlockStateProperties.WATERLOGGED.getName() + "=")) {
                        val.put(state.setValue(BlockStateProperties.WATERLOGGED, !state.getValue(BlockStateProperties.WATERLOGGED)), BlockData.BlockStateMeta.of(null, entry.getValue()));
                    }

                    val.put(state, BlockData.BlockStateMeta.of(null, entry.getValue()));
                }
            }
        }

        return val;
    }

    @Override
    public boolean virtual() {
        return true;
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
        public Vector3f origin() {
            return origin == null ? Vec3.ZERO.toVector3f() : origin;
        }
    }
}
