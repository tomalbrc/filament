package de.tomalbrc.filament.mixin.behaviour.oxidizable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Oxidizable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChangeOverTimeBlock.class)
public interface ChangeOverTimeBlockMixin {
    @WrapOperation(method = "getNextState", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState filament$oxidizableState(ServerLevel instance, BlockPos pos, Operation<BlockState> original) {
        var state = original.call(instance, pos);
        if (state.getBlock() instanceof BehaviourHolder behaviourHolder) {
            Oxidizable oxidizable;
            if ((oxidizable = behaviourHolder.get(Behaviours.OXIDIZABLE)) != null) {
                return switch (oxidizable.getConfig().weatherState) {
                    case UNAFFECTED -> Blocks.COPPER_BLOCK.defaultBlockState();
                    case EXPOSED -> Blocks.EXPOSED_COPPER.defaultBlockState();
                    case WEATHERED -> Blocks.WEATHERED_COPPER.defaultBlockState();
                    case OXIDIZED -> Blocks.OXIDIZED_COPPER.defaultBlockState();
                };
            }
        }

        return state;
    }
}
