package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

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

            if (clickedState.getBlock() instanceof PolymerBlock polymerBlock && polymerBlock.getPolymerBlockState(clickedState, player).getBlock() instanceof NoteBlock) {
                Util.playBlockPlaceSound(player, pos, placementState.getSoundType());
            }
        }
    }

    @Inject(method = "appendHoverText", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"), cancellable = true)
    void filament$fixupAppendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (this.getBlock() == null) {
            ci.cancel();
        }
    }
}
