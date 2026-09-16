package de.tomalbrc.filament.mixin.behaviour.strippable;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Strippable;
import de.tomalbrc.filament.registry.StrippableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "useOn", at = @At(value = "HEAD"), cancellable = true)
    private void filament$onGetStripped(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide()) return;

        var player = context.getPlayer();
        var level = context.getLevel();
        var blockState = level.getBlockState(context.getClickedPos());
        var blockPos = context.getClickedPos();

        var newBlock = StrippableRegistry.get(blockState.getBlock());
        if (newBlock != null) {
            var newState = newBlock.withPropertiesOf(blockState);
            level.setBlockAndUpdate(context.getClickedPos(), newState);
            // TODO: 26.3 - check if level event should be sent to all players + sound

            Strippable strippable = blockState.getBlock().get(Behaviours.STRIPPABLE);
            level.playSound(player, blockPos, SoundEvent.createVariableRangeEvent(strippable.getConfig().sound), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (strippable.getConfig().scrape) {
                level.levelEvent(null, LevelEvent.PARTICLES_SCRAPE, blockPos, 0);

                if (blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    ((ServerPlayer)player).connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_SCRAPE, ChestBlock.getConnectedBlockPos(blockPos, blockState), 0, false));
                }
            }

            if (strippable.getConfig().scrapeWax) {
                level.levelEvent(null, LevelEvent.PARTICLES_WAX_OFF, blockPos, 0);

                if (blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    ((ServerPlayer)player).connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_WAX_OFF, ChestBlock.getConnectedBlockPos(blockPos, blockState), 0, false));
                }
            }

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

            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
