package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.util.BlockUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "place", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At("TAIL"))
    private void filament$onPlaceBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext context2, BlockState placementState, BlockPos blockPos, Level level) {
        BlockItem blockItem = (BlockItem) (Object) this;

        if (context.getPlayer() instanceof ServerPlayer player && !player.isSecondaryUseActive() &&
                !context.replacingClickedOnBlock() &&
                !(blockItem instanceof PolymerItem)
        ) {
            BlockPos pos = context.getClickedPos();
            BlockPos clickedPos = pos.relative(context.getClickedFace().getOpposite());
            BlockState clickedState = context.getLevel().getBlockState(clickedPos);

            if (clickedState.getBlock() instanceof PolymerBlock polymerBlock && polymerBlock.getPolymerBlockState(clickedState, PacketContext.create(player)).getBlock() instanceof NoteBlock) {
                BlockUtil.playBlockPlaceSound(player, pos, placementState.getSoundType());
            }
        }
    }
}
