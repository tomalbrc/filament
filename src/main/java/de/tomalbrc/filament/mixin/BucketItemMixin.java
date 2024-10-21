package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.util.FakeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin implements FakeItem {
    @Shadow public abstract InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand);

    @Shadow @Final private Fluid content;

    @Shadow protected abstract void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos);

    @Inject(cancellable = true, method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filament$preventBucketInteraction(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Local BlockState blockState, @Local ItemStack itemStack) {
        if (blockState.getBlock() instanceof SimpleBlock && !blockState.hasProperty(BlockStateProperties.WATERLOGGED))
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
    }


    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        var bs = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        if (bs.getBlock() instanceof SimpleBlock simpleBlock && simpleBlock.getPolymerBlockState(bs, (ServerPlayer) useOnContext.getPlayer()).getBlock() instanceof NoteBlock) {
            Fluid content1 = content;
            var res = this.use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand()).getResult();
            if (res.consumesAction()) {
                if (content1 == Fluids.EMPTY) {
                    playEmptySound(null, useOnContext.getLevel(), useOnContext.getClickedPos());
                } else {
                    simpleBlock.getPickupSound().ifPresent((soundEvent) -> {
                        useOnContext.getLevel().playSound(null, useOnContext.getClickedPos(), soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    });
                }
            }
            return res;
        }
        return InteractionResult.PASS;
    }
}
