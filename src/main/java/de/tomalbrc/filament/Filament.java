package de.tomalbrc.filament;

import com.mojang.logging.LogUtils;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.command.DyeCommand;
import de.tomalbrc.filament.command.HatCommand;
import de.tomalbrc.filament.command.PickCommand;
import de.tomalbrc.filament.data.ItemGroupData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.*;
import de.tomalbrc.filament.util.*;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class Filament implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.markAsRequired();
        Behaviours.init();
        EntityRegistry.register();

        if (FilamentConfig.getInstance().commands) {
            CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
                HatCommand.register(dispatcher);
                DyeCommand.register(dispatcher);
                PickCommand.register(dispatcher);
            });
        }

        if (FilamentConfig.getInstance().emissive_shader) {
            FilamentShaderUtil.registerCallback();
        }

        PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.register(VirtualDestroyStage::updateState);
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                ((VirtualDestroyStage.ServerGamePacketListenerExtF) serverPlayer.connection).filament$getVirtualDestroyStage().setState(-1);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                if (DecorationRegistry.isDecoration(blockState) && world.getBlockEntity(pos) instanceof DecorationBlockEntity decorationBlockEntity) {
                    return decorationBlockEntity.decorationInteract((ServerPlayer) player, hand, hitResult.getLocation());
                }
            }

            return InteractionResult.PASS;
        });

        ItemGroupRegistry.register(new ItemGroupData(Constants.ITEM_GROUP_ID, ResourceLocation.withDefaultNamespace("diamond"), "<c:blue>Filament Items"));
        ItemGroupRegistry.register(new ItemGroupData(Constants.BLOCK_GROUP_ID, ResourceLocation.withDefaultNamespace("furnace"), "<c:blue>Filament Blocks"));
        ItemGroupRegistry.register(new ItemGroupData(Constants.DECORATION_GROUP_ID, ResourceLocation.withDefaultNamespace("lantern"), "<c:blue>Filament Decorations"));

        FilamentReloadUtil.registerEarlyReloadListener(new FilamentAssetReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new ModelRegistry.AjModelReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new BlockRegistry.BlockDataReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new DecorationRegistry.DecorationDataReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new ItemRegistry.ItemDataReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new ItemGroupRegistry.ItemGroupDataReloadListener());

        FilamentRPUtil.registerCallback();

        VirtualDestroyStage.destroy(null);
    }
}
