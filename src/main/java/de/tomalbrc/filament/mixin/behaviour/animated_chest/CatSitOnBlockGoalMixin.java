package de.tomalbrc.filament.mixin.behaviour.animated_chest;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSitOnBlockGoal.class)
public class CatSitOnBlockGoalMixin {
    @Inject(method = "isValidTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0), cancellable = true)
    private void filament$chest(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(name = "blockState") BlockState blockState) {
        if (blockState.getBlock() instanceof DecorationBlock decorationBlock && decorationBlock.has(Behaviours.ANIMATED_CHEST)) {
            var decorationBlockEntity = level.getBlockEntity(pos);
            if (decorationBlockEntity != null) {
                var ac = ((DecorationBlockEntity) decorationBlockEntity).getOrThrow(Behaviours.ANIMATED_CHEST);
                if (!ac.container.hasViewers()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
