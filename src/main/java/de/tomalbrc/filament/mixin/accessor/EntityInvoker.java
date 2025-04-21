package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker(value = "applyMovementEmissionAndPlaySound")
    void invokeApplyMovementEmissionAndPlaySound(Entity.MovementEmission movementEmission, Vec3 vec3, BlockPos blockPos, BlockState blockState);
}
