package de.tomalbrc.filament.mixin.behaviour.strippable;

import de.tomalbrc.filament.registry.StrippableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @Inject(method = "evaluateNewBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/AxeItem;getStripped(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"), cancellable = true)
    private void filament$onGetStripped(Level level, BlockPos blockPos, Player player, BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (StrippableRegistry.has(blockState.getBlock())) {
            var newState = StrippableRegistry.get(blockState.getBlock()).withPropertiesOf(blockState);
            level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            var lootId = StrippableRegistry.getLootTable(blockState.getBlock());
            if (lootId != null) {
                var tableReference = level.registryAccess().get(ResourceKey.create(Registries.LOOT_TABLE, lootId));
                var table = tableReference.orElseThrow().value();
                table.getRandomItems(new LootParams.Builder((ServerLevel) level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                        .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.BLOCK_STATE, blockState)
                        .withLuck(player.getLuck())
                        .create(LootContextParamSets.BLOCK), item -> Block.popResource(level, blockPos, item));
            }
            cir.setReturnValue(Optional.of(newState));
        }
    }
}
