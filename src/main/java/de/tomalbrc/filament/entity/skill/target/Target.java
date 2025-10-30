package de.tomalbrc.filament.entity.skill.target;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Target {
    private BlockState blockState;
    private BlockPos blockPos;
    private Vec3 position;
    private Entity entity;
    public Level level;

    public static Target of() {
        return new Target();
    }

    public static Target of(Level level, BlockState blockState, BlockPos pos) {
        Target target = new Target();
        target.blockState = blockState;
        target.blockPos = pos;
        return target;
    }

    public static Target of(Entity entity) {
        Target target = new Target();
        target.entity = entity;
        target.position = entity.position();
        target.level = entity.level();
        return target;
    }

    public Entity getEntity() {
        return entity;
    }

    public Vec3 getPosition() {
        return position;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    boolean isEntity() {
        return this.entity != null;
    }

    boolean isBlock() {
        return this.blockState != null;
    }

    public Level level() {
        return level;
    }
}
