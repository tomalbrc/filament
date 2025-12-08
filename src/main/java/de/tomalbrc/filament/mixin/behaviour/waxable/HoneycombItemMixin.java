package de.tomalbrc.filament.mixin.behaviour.waxable;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.registry.WaxableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @Inject(method = "getWaxed", at = @At("RETURN"), cancellable = true)
    private static void filament$customWaxable(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (cir.getReturnValue().isEmpty() && blockState.getBlock().isFilamentBlock() && blockState.getBlock().has(Behaviours.WAXABLE)) {
            cir.setReturnValue(Optional.of(WaxableRegistry.getWaxed(blockState.getBlock()).withPropertiesOf(blockState)));
        }
    }

    @Inject(method = "method_34719", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V", ordinal = 0))
    private static void filament$broadcastToPlayer(UseOnContext useOnContext, BlockPos blockPos, Level level, BlockState blockState, BlockState blockState2, CallbackInfoReturnable<InteractionResult> cir, @Local Player player) {
        if (!level.isClientSide() && WaxableRegistry.getPrevious(blockState2.getBlock()) != null) {
            ((ServerPlayer)player).connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, blockPos, 0, false));

            if (blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                ((ServerPlayer)player).connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, ChestBlock.getConnectedBlockPos(blockPos, blockState2), 0, false));
            }
        }
    }
}
