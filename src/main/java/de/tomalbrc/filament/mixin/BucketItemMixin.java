package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow public abstract InteractionResult use(Level level, Player player, InteractionHand interactionHand);

    @Inject(cancellable = true, method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filament$preventBucketInteraction(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir, @Local BlockState blockState, @Local ItemStack itemStack) {
        if (blockState.getBlock().isFilamentBlock() && !blockState.hasProperty(BlockStateProperties.WATERLOGGED))
            cir.setReturnValue(InteractionResult.FAIL);
    }
}
