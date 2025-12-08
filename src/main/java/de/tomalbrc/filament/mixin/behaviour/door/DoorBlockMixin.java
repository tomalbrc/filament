package de.tomalbrc.filament.mixin.behaviour.door;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DoorBlock.class)
public class DoorBlockMixin {
    @WrapOperation(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0))
    private Block filament$onUpdateShape(BlockState instance, Operation<Block> original) {
        if (instance.getBlock().isFilamentBlock()) {
            return Blocks.OAK_DOOR;
        }

        return original.call(instance);
    }
}
