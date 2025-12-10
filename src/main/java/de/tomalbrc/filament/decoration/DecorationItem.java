package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DecorationItem extends SimpleBlockItem implements PolymerItem, BehaviourHolder {
    final private DecorationData decorationData;

    public DecorationItem(Block block, DecorationData decorationData, Item.Properties properties) {
        super(properties, block, decorationData);
        this.initBehaviours(decorationData.behaviour());
        this.decorationData = decorationData;
    }

    public DecorationData getDecorationData() {
        return this.decorationData;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        if (this.decorationData.vanillaItem().components().has(DataComponents.DYED_COLOR) || this.decorationData.components().has(DataComponents.DYED_COLOR)) {
            consumer.accept(Component.literal("§9Dyeable"));
        }

        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't use decoration Item: Missing decoration data!");
            return InteractionResult.FAIL;
        }

        DecorationProperties properties = decorationData.properties();

        var clickedState = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        var replaceable = clickedState.canBeReplaced();

        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction, actualDir = direction = replaceable ? Direction.UP : useOnContext.getClickedFace();
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        Level level = useOnContext.getLevel();

        boolean propertyPlaceCheck = properties.placement.canPlace(direction);
        if (!propertyPlaceCheck && properties.placement.floor()) {
            direction = Direction.UP;
            propertyPlaceCheck = properties.placement.canPlace(direction);
        }

        if (!propertyPlaceCheck && properties.placement.ceiling()) {
            direction = Direction.DOWN;
            propertyPlaceCheck = properties.placement.canPlace(direction);
        }

        DecorationBlock block = DecorationRegistry.getDecorationBlock(decorationData.id());
        BlockState blockState = block.getStateForPlacement(new BlockPlaceContext(useOnContext));

        boolean forceReplace = replaceable || clickedState.canBeReplaced(new BlockPlaceContext(useOnContext));
        BlockPos relativeBlockPos;
        if (forceReplace) {
            relativeBlockPos = blockPos;
        } else {
            relativeBlockPos = blockPos.relative(direction);
        }

        assert blockState != null;
        float angle = block.getVisualRotationYInDegrees(blockState);

        if (!this.getBlock().isEnabled(level.enabledFeatures()) || player == null || !this.mayPlace(player, direction, itemStack, relativeBlockPos) || !propertyPlaceCheck) {
            return InteractionResult.FAIL;
        } else if ((forceReplace || this.canPlaceAt(level, relativeBlockPos, angle)) && itemStack.getItem() instanceof DecorationItem) {
            DecorationItem.place(itemStack, level, blockState, relativeBlockPos, actualDir, direction, useOnContext, forceReplace);

            player.startUsingItem(useOnContext.getHand());
            itemStack.consume(1, player);

            SoundEvent placeSound = properties.blockBase.defaultBlockState().getSoundType().getPlaceSound();
            level.playSound(null, blockPos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        return !player.level().isOutsideBuildHeight(blockPos) && player.mayUseItemAt(blockPos, direction, itemStack);
    }

    /**
     * Check if multi-block structure can be placed
     */
    private boolean canPlaceAt(Level level, BlockPos blockPos, float angle) {
        if (!level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }

        if (decorationData.hasBlocks()) {
            boolean[] canPlace = new boolean[]{true};
            DecorationUtil.forEachRotated(decorationData.blocks(), blockPos, angle, blockPos2 -> {
                if (!level.getBlockState(blockPos2).canBeReplaced()) {
                    canPlace[0] = false;
                }
            });
            return canPlace[0];
        }

        return true;
    }

    public static void place(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, Direction direction, UseOnContext useOnContext) {
        place(itemStack, level, blockState, blockPos, direction, direction, useOnContext, false);
    }

    public static void place(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, Direction placeDirection, Direction direction, UseOnContext useOnContext, boolean forceReplace) {
        if (!(itemStack.getItem() instanceof DecorationItem decorationItem)) {
            Filament.LOGGER.error("Tried to place non-decoration item as decoration! Item: {}", itemStack.getItem().builtInRegistryHolder().key().identifier());
            return;
        }

        DecorationData decorationData = decorationItem.decorationData;
        if (decorationData.hasBlocks() && decorationData.countBlocks() == 0)
            Filament.LOGGER.warn("Found block data with potentially invalid blocks for {} while trying to place", decorationData.id());

        DecorationBlock block = (DecorationBlock) blockState.getBlock();
        if (decorationData.hasBlocks() && decorationData.countBlocks() > 1) {
            DecorationUtil.forEachRotated(decorationData.blocks(), blockPos, block.getVisualRotationYInDegrees(blockState), blockPos2 -> {
                level.destroyBlock(blockPos2, false);

                var offsetState = block.getStateForPlacement(BlockPlaceContext.at(new BlockPlaceContext(useOnContext), blockPos2, placeDirection));
                level.setBlockAndUpdate(blockPos2, offsetState);

                if (offsetState != null) {
                    offsetState.getBlock().setPlacedBy(level, blockPos2, offsetState, useOnContext.getPlayer(), itemStack);
                    if (useOnContext.getPlayer() != null) CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) useOnContext.getPlayer(), blockPos, itemStack);
                }

                if (!forceReplace && decorationData.requiresEntityBlock() && level.getBlockEntity(blockPos2) instanceof DecorationBlockEntity decorationBlockEntity) {
                    decorationBlockEntity.setMain(new BlockPos(blockPos2).subtract(blockPos));
                    decorationBlockEntity.setDirection(direction);
                    if (decorationBlockEntity.isMain()) updateComponents(decorationBlockEntity, itemStack);
                    decorationBlockEntity.attach(level.getChunkAt(blockPos));
                }
            });
        } else {
            level.setBlockAndUpdate(blockPos, blockState);
            blockState.getBlock().setPlacedBy(level, blockPos, blockState, useOnContext.getPlayer(), itemStack);
            if (useOnContext.getPlayer() != null) CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) useOnContext.getPlayer(), blockPos, itemStack);

            if (!forceReplace && decorationData.requiresEntityBlock() && level.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                decorationBlockEntity.setMain(BlockPos.ZERO);
                decorationBlockEntity.setDirection(direction);
                updateComponents(decorationBlockEntity, itemStack);
                decorationBlockEntity.attach(level.getChunkAt(blockPos));
            }
        }
    }

    public static void updateComponents(DecorationBlockEntity blockEntity, ItemStack itemStack) {
        blockEntity.applyComponentsFromItemStack(itemStack);
        blockEntity.setChanged();
    }
}
