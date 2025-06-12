package de.tomalbrc.filament.util;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class UpgradeUtil {
    // FIXME: I should really not do this: write datafixer
    public static void upgradeDecoration1to2(DecorationBlockEntity blockEntity, Level level, BlockPos pos) {
        if (blockEntity.getDecorationData().behaviour().has(Behaviours.ROTATING)) {
            int rot = blockEntity.rotation;
            if (!blockEntity.getDirection().getAxis().isVertical()) {
                rot = Util.SEGMENTED_ANGLE8.fromDegrees(getVisualRotationYInDegrees(blockEntity.getDirection(), rot));
            }
            level.setBlock(pos, blockEntity.getBlockState().setValue(BlockUtil.ROTATION, rot), Block.UPDATE_NONE);
        }
    }

    public static float getVisualRotationYInDegrees(Direction direction, int rotation) {
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }
}
