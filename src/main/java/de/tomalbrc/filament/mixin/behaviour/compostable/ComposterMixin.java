package de.tomalbrc.filament.mixin.behaviour.compostable;

import de.tomalbrc.filament.behaviour.Behaviours;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class ComposterMixin {
    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ComposterBlock;addLayer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/component/Compostable;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void filament$onUseItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        var comp = itemStack.getItem().get(Behaviours.COMPOSTABLE);
        if (comp != null) {
            if (level.getRandom().nextInt(100) <= comp.getConfig().chance) {
                int fillLevel = state.getValue(ComposterBlock.LEVEL);
                int newLevel = Mth.clamp(fillLevel + 1, 0, 7);
                BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
                level.setBlockAndUpdate(pos, newState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
                if (newLevel == 7) {
                    level.scheduleTick(pos, state.getBlock(), 20);
                }

                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
