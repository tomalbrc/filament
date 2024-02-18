package de.tomalbrc.filament;

import com.mojang.logging.LogUtils;
import de.tomalbrc.filament.command.DyeCommand;
import de.tomalbrc.filament.command.PickCommand;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.filament.*;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentAssetReloadListener;
import de.tomalbrc.filament.util.FilamentRPUtil;
import de.tomalbrc.filament.util.FilamentReloadUtil;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class Filament implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(Constants.MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        EntityRegistry.register();
        EnchantmentRegistry.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            DyeCommand.register(dispatcher);
            PickCommand.register(dispatcher);
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

        FilamentReloadUtil.registerEarlyReloadListener(new FilamentAssetReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new AjModelRegistry.AjModelReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new BlockRegistry.BlockDataReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new DecorationRegistry.DecorationDataReloadListener());
        FilamentReloadUtil.registerEarlyReloadListener(new ItemRegistry.ItemDataReloadListener());

        FilamentRPUtil.registerCallback();
    }
}
