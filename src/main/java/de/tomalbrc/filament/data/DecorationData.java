package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public final class DecorationData extends Data {
    private final @NotNull ResourceLocation model;
    private final @Nullable List<BlockConfig> blocks;
    private final @Nullable Vector2f size;
    private final @Nullable DecorationProperties properties;

    public DecorationData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable ItemResource itemResource,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @Nullable DecorationProperties properties,
            @NotNull ResourceLocation model,
            @Nullable List<BlockConfig> blocks,
            @Nullable Vector2f size
    ) {
        super(id, vanillaItem, itemResource, behaviourConfig, components, itemGroup);
        this.model = model;
        this.blocks = blocks;
        this.size = size;
        this.properties = properties;
    }

    @NotNull
    public DecorationProperties properties() {
        if (properties == null) {
            return DecorationProperties.EMPTY;
        }
        return properties;
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

        int c = 0;
        for (BlockConfig block : this.blocks) {
            c += (int) (block.size().x() * block.size().y() * block.size().z());
        }
        return c;
    }

    public boolean isSimple() {
        boolean singleBlock = (!this.hasBlocks() || Util.barrierDimensions(Objects.requireNonNull(this.blocks()), 0).equals(1, 1));
        boolean hasBehaviour = !this.behaviour().isEmpty();
        boolean canBeDyed = this.vanillaItem != null && (vanillaItem == Items.LEATHER_HORSE_ARMOR || vanillaItem == Items.FIREWORK_STAR);
        boolean groundOnly = this.properties != null && !(this.properties.placement.wall() || this.properties.placement.ceiling());

        return groundOnly && !canBeDyed && !hasBehaviour && (singleBlock || this.size != null);
    }

    public @NotNull ResourceLocation model() {
        return model;
    }

    public @Nullable List<BlockConfig> blocks() {
        return blocks;
    }

    public @Nullable Vector2f size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DecorationData) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.model, that.model) &&
                Objects.equals(this.itemResource, that.itemResource) &&
                Objects.equals(this.vanillaItem, that.vanillaItem) &&
                Objects.equals(this.blocks, that.blocks) &&
                Objects.equals(this.size, that.size) &&
                Objects.equals(this.properties, that.properties) &&
                Objects.equals(this.behaviour, that.behaviour) &&
                Objects.equals(this.components, that.components) &&
                Objects.equals(this.group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, itemResource, vanillaItem, blocks, size, properties, behaviour, components, group);
    }

    @Override
    public String toString() {
        return "DecorationData[" +
                "id=" + id + ", " +
                "model=" + model + ", " +
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
                              Vector3f size,
                              BlockState block) {
    }
}
