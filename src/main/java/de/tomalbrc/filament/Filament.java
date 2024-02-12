package de.tomalbrc.filament;

import com.mojang.logging.LogUtils;
import de.tomalbrc.filament.command.DyeCommand;
import de.tomalbrc.filament.command.PickCommand;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.*;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

public class Filament implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(Constants.MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        BlockRegistry.register();
        ItemRegistry.register();
        EntityRegistry.register();
        DecorationRegistry.register();
        EnchantmentRegistry.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            DyeCommand.register(dispatcher);
            PickCommand.register(dispatcher);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() instanceof DecorationBlock && world.getBlockEntity(pos) instanceof DecorationBlockEntity decorationBlockEntity) {
                    return decorationBlockEntity.decorationInteract((ServerPlayer) player, hand, hitResult.getLocation());
                }
            }

            return InteractionResult.PASS;
        });

        LOGGER.info("---------------------");
        LOGGER.info("Items registered: " + ItemRegistry.REGISTERED_ITEMS);
        LOGGER.info("Blocks registered: " + BlockRegistry.REGISTERED_BLOCKS);
        LOGGER.info("Decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS);
        LOGGER.info("Decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES);
        LOGGER.info("---------------------");

        /*
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
        PoymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            resourcePackBuilder.addData( d.id().getNamespace() +"/model/block/", BlockModelGenerators.createPillarBlockUVLocked(null, null, null).get().toString().getBytes(StandardCharsets.UTF_8));

        });*/
    }
}
