package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.config.data.DecorationData;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DecorationItem extends Item implements PolymerItem {
    final private DecorationData decorationData;
    final private PolymerModelData modelData;

    public DecorationItem(DecorationData decorationData) {
        super(decorationData.properties() != null ? decorationData.properties().toItemProperties() : new Item.Properties().stacksTo(16));
        this.decorationData = decorationData;
        this.modelData = this.decorationData.requestModel();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (this.decorationData.vanillaItem() == Items.LEATHER_HORSE_ARMOR) {
            tooltip.add(Component.literal("§9Dyeable"));
        }

        if (this.decorationData.properties() != null) {
            this.decorationData.properties().appendHoverText(tooltip);
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayer player) {
        return modelData.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        return modelData.value();
    }

    @Override
    public boolean showDefaultNameInItemFrames() {
        return false;
    }

    public static float getVisualRotationYInDegrees(Direction direction, int rotation) {
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't use decoration Item: Missing decoration data!");
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockPos relativeBlockPos = blockPos.relative(direction);
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        Level level = useOnContext.getLevel();

        int rotation = 0;
        if (decorationData.properties() != null && decorationData.properties().rotate) {
            if (decorationData.properties().rotateSmooth) {
                rotation = Util.SEGMENTED_ANGLE8.fromDegrees(useOnContext.getRotation()-180);
            } else {
                rotation = Util.SEGMENTED_ANGLE8.fromDirection(useOnContext.getHorizontalDirection().getOpposite());
            }
        }

        boolean propertyPlaceCheck = true;
        if (decorationData.properties() != null) {
            propertyPlaceCheck = decorationData.properties().placement.canPlace(direction);
        }

        if (player == null || !this.mayPlace(player, direction, itemStack, relativeBlockPos)) {
            return InteractionResult.FAIL;
        } else if (!propertyPlaceCheck) {
            return InteractionResult.FAIL;
        } else if (this.canPlaceAt(level, relativeBlockPos, useOnContext.getHorizontalDirection(), useOnContext.getClickedFace()) && itemStack.getItem() instanceof DecorationItem) {
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, useOnContext.getHand(), itemStack, new BlockHitResult(useOnContext.getClickLocation(), useOnContext.getClickedFace(), useOnContext.getClickedPos(), useOnContext.isInside()));

            if (decorationData.blocks() != null) {
                int finalRotation = rotation;
                Util.forEachRotated(decorationData.blocks(), relativeBlockPos, this.getVisualRotationYInDegrees(direction, rotation), blockPos2 -> {
                    boolean w = level.getBlockState(blockPos2).is(Blocks.WATER);
                    level.destroyBlock(blockPos2, true);

                    BlockState blockState = BlockRegistry.DECORATION_BLOCK.getStateForPlacement(blockPlaceContext);
                    if (decorationData.properties() != null && decorationData.properties().isLightSource()) {
                        blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission);
                    }
                    blockState = blockState.setValue(DecorationBlock.WATERLOGGED, w);

                    level.setBlockAndUpdate(blockPos2, blockState);
                    if (level.getBlockEntity(blockPos2) instanceof DecorationBlockEntity decorationBlockEntity) {
                        decorationBlockEntity.setMain(relativeBlockPos);
                        decorationBlockEntity.setItem(itemStack.copyWithCount(1));
                        decorationBlockEntity.setRotation(finalRotation);
                        decorationBlockEntity.setDirection(direction);
                        decorationBlockEntity.setupBehaviour(decorationData);
                        decorationBlockEntity.attach((ServerLevel) level);
                    }
                });
            } else {
                BlockState blockState = BlockRegistry.DECORATION_BLOCK.getStateForPlacement(blockPlaceContext);
                blockState = blockState.setValue(DecorationBlock.PASSTHROUGH, true);

                if (decorationData.properties() != null && decorationData.properties().isLightSource()) {
                    blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission);
                }

                level.setBlockAndUpdate(relativeBlockPos, blockState);
                if (level.getBlockEntity(relativeBlockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                    decorationBlockEntity.setMain(relativeBlockPos);
                    decorationBlockEntity.setItem(itemStack.copyWithCount(1));
                    decorationBlockEntity.setRotation(rotation);
                    decorationBlockEntity.setDirection(direction);
                    decorationBlockEntity.setupBehaviour(decorationData);
                    decorationBlockEntity.attach((ServerLevel) level);
                }
            }

            player.startUsingItem(useOnContext.getHand());
            itemStack.shrink(1);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.FAIL;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        return !player.level().isOutsideBuildHeight(blockPos) && player.mayUseItemAt(blockPos, direction, itemStack);
    }

    /**
     * Check if multi-block structure can be placed
     */
    private boolean canPlaceAt(Level level, BlockPos blockPos, Direction direction, Direction clickedFace) {
        if (!level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }

        if (decorationData.blocks() != null) {
            float angle;
            angle = Util.SEGMENTED_ANGLE8.toDegrees(Util.SEGMENTED_ANGLE8.fromDirection(direction.getOpposite()));

            // :concern:
            AtomicBoolean canPlace = new AtomicBoolean(true);
            Util.forEachRotated(decorationData.blocks(), blockPos, angle, blockPos2 -> {
                if (!level.getBlockState(blockPos2).canBeReplaced()) {
                    canPlace.set(false);
                }
            });
            return canPlace.get();
        }

        return true;
    }
}
