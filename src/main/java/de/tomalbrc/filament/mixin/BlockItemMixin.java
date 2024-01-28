package de.tomalbrc.filament.mixin;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import de.tomalbrc.filament.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"))
    private void onPlaceBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext context2, BlockState placementState, BlockPos blockPos, Level level) {
        BlockItem blockItem = (BlockItem) (Object) this;

        if (context.getPlayer() instanceof ServerPlayer player && !player.isSecondaryUseActive() &&
            !context.replacingClickedOnBlock() &&
            !(blockItem instanceof PolymerItem)
        ) {
            BlockPos pos = context.getClickedPos();
            BlockPos clickedPos = pos.relative(context.getClickedFace().getOpposite());
            BlockState clickedState = context.getLevel().getBlockState(clickedPos);

            if (clickedState.getBlock() instanceof PolymerBlock polymerBlock && polymerBlock.getPolymerBlock(clickedState, player) instanceof NoteBlock) {
                Util.playBlockPlaceSound(player, pos, placementState.getSoundType());
            }
        }
    }
}
