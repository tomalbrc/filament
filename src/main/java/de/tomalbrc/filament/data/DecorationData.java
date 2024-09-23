package de.tomalbrc.filament.data;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
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

public record DecorationData(
        @NotNull ResourceLocation id,
        @NotNull ResourceLocation model,

        @Nullable Item vanillaItem,

        @Nullable List<BlockConfig> blocks,

        @Nullable Vector2f size,

        @Nullable DecorationProperties properties,

        @SerializedName("behaviour")
        @Nullable BehaviourConfigMap behaviourConfig,
        @Nullable DataComponentMap components,
        @SerializedName("group")
        @Nullable ResourceLocation itemGroup
) {
    @Override
    @NotNull
    public DecorationProperties properties() {
        if (properties == null) {
            return DecorationProperties.EMPTY;
        }
        return properties;
    }

    @Override
    @NotNull
    public DataComponentMap components() {
        if (components == null) {
            return DataComponentMap.EMPTY;
        }
        return components;
    }

    @Override
    @NotNull
    public Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    public PolymerModelData requestModel() {
        return PolymerResourcePackUtils.requestModel(vanillaItem != null ? vanillaItem : Items.GUNPOWDER, model);
    }

    public boolean isContainer() {
        return this.behaviourConfig != null && this.behaviourConfig.has(Behaviours.CONTAINER);
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
        boolean hasBehaviour = this.behaviourConfig() != null;
        boolean canBeDyed = this.vanillaItem != null && (vanillaItem == Items.LEATHER_HORSE_ARMOR || vanillaItem == Items.FIREWORK_STAR);
        boolean groundOnly = this.properties != null && !this.properties.placement.wall() && !this.properties.placement.ceiling();

        return groundOnly && !canBeDyed && !hasBehaviour && (singleBlock || this.size != null);
    }

    public boolean isCosmetic() {
        return this.behaviourConfig != null && this.behaviourConfig.has(Behaviours.COSMETIC);
    }

    public record BlockConfig(Vector3f origin,
                       Vector3f size,
                       BlockState block) { }
}
