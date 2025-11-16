package de.tomalbrc.filament.mixin.behaviour.bed;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "findRespawnAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0), cancellable = true)
    private static void filament$findRespawnAndUseSpawnBlock(ServerLevel serverLevel, BlockPos blockPos, float f, boolean bl, boolean bl2, CallbackInfoReturnable<Optional<ServerPlayer.RespawnPosAngle>> cir) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.getBlock() instanceof DecorationBlock decorationBlock && decorationBlock.getDecorationData().behaviour().has(Behaviours.BED) && BedBlock.canSetSpawn(serverLevel)) {
            var v = BedBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos, blockState.getValue(BedBlock.FACING), f).map((vec3) -> ServerPlayer.RespawnPosAngle.of(vec3, blockPos));
            cir.setReturnValue(v);
        }
    }
}
