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
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class DecorationData extends Data {
    private final @Nullable ResourceLocation model;
    private final @Nullable List<BlockConfig> blocks;
    private final @Nullable Vector2f size;
    private final @Nullable DecorationProperties properties;

    public DecorationData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable ItemResource itemResource,
            @Nullable ResourceLocation itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @Nullable DecorationProperties properties,
            @Nullable ResourceLocation model,
            @Nullable List<BlockConfig> blocks,
            @Nullable Vector2f size
    ) {
        super(id, vanillaItem, translations, itemResource, itemModel, behaviourConfig, components, itemGroup);
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
        boolean groundOnly = !this.properties().placement.wall() && !this.properties().placement.ceiling();

        return groundOnly && !canBeDyed && !hasBehaviour && (singleBlock || this.size != null);
    }

    public @NotNull ResourceLocation model() {
        if (model == null) {
            assert itemResource != null;
            return itemResource.models().get("default");
        } else {
            return model;
        }
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
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public boolean isLightEnabled() {
        return properties().mayBeLightSource() || behaviour().has(Behaviours.LAMP);
    }

    public boolean hasLightBehaviours() {
        return behaviour().has(Behaviours.LAMP);
    }

    public record BlockConfig(Vector3f origin,
                              Vector3f size,
                              BlockState block) {
    }
}
