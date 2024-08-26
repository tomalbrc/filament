package de.tomalbrc.filament.data;

import de.tomalbrc.filament.data.behaviours.block.BehaviourMap;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.util.Constants;
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

public record DecorationData(
        @NotNull ResourceLocation id,
        @NotNull ResourceLocation model,

        @Nullable Item vanillaItem,

        @Nullable List<BlockConfig> blocks,

        @Nullable Vector2f size,

        @Nullable DecorationProperties properties,

        @Nullable BehaviourMap behaviour,
        @Nullable DataComponentMap components
) {
    public PolymerModelData requestModel() {
        return PolymerResourcePackUtils.requestModel(vanillaItem != null ? vanillaItem : Items.GUNPOWDER, model);
    }

    public boolean isSeat() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.SEAT) != null;
    }

    public boolean isContainer() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.CONTAINER) != null;
    }

    public boolean hasAnimation() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.ANIMATION) != null;
    }

    public boolean hasBlocks() {
        return this.blocks != null;
    }

    public boolean isShowcase() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.SHOWCASE) != null;
    }

    public boolean isLock() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.LOCK) != null;
    }

    public boolean isFuel() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.FUEL) != null;
    }

    public boolean isSimple() {
        return false;
        // TODO: itemStack is not correct/missing data since we get in from the decoration data id...
        // so dyed color will be missing from the displayed item
        //return ((!this.hasBlocks() || Util.barrierDimensions(this.blocks(), 0).equals(1, 1)) && this.behaviour() == null) || this.size != null;
    }

    public boolean isCosmetic() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.COSMETIC) != null;
    }

    public record BlockConfig(Vector3f origin,
                       Vector3f size,
                       BlockState block) { }
}
