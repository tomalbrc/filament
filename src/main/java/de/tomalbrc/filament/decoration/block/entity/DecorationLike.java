package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Common interface for placed decorations, both block-based and entity-based
 */
public interface DecorationLike extends BehaviourHolder {

    Vec3 getDecorationPosition();

    float getVisualRotationYInDegrees();

    Direction getDirection();

    FilamentDecorationHolder getOrCreateHolder();

    ItemStack getItem();

    Level getLevel();

    DecorationData getDecorationData();

    void setChanged();

    default BlockPos getBlockPos() {
        return BlockPos.containing(getDecorationPosition());
    }

    boolean isRemoved();

    InteractionResult interact(Player player, InteractionHand hand, Vec3 location);

    DataComponentMap components();

    @Nullable
    default BlockState getBlockState() {
        return null;
    }

    default void destroyStructure(boolean dropItem) {}
}