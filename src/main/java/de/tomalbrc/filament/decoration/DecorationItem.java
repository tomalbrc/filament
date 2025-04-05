package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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

    public static float getVisualRotationYInDegrees(Direction direction, int rotation) {
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        var res = super.useOn(useOnContext);
        if (res.consumesAction())
            return res;

        if (decorationData == null) {
            Filament.LOGGER.warn("Can't use decoration Item: Missing decoration data!");
            return InteractionResult.FAIL;
        }

        DecorationProperties properties = decorationData.properties();

        var replaceable = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos()).canBeReplaced();

        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = replaceable ? Direction.UP : useOnContext.getClickedFace();
        BlockPos relativeBlockPos = replaceable ? blockPos : blockPos.relative(direction);
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        Level level = useOnContext.getLevel();

        int rotation = 0;
        if (properties.rotate) {
            if (properties.rotateSmooth) {
                rotation = Util.SEGMENTED_ANGLE8.fromDegrees(useOnContext.getRotation()-180);
            } else {
                rotation = Util.SEGMENTED_ANGLE8.fromDirection(useOnContext.getHorizontalDirection().getOpposite());
            }
        }

        boolean propertyPlaceCheck = properties.placement.canPlace(direction);
        if (!propertyPlaceCheck && properties.placement.floor() && !level.getBlockState(relativeBlockPos.relative(Direction.DOWN)).isAir()) {
            direction = Direction.UP;
            propertyPlaceCheck = properties.placement.canPlace(direction);
        }

        if (!propertyPlaceCheck && properties.placement.ceiling() && !level.getBlockState(relativeBlockPos.relative(Direction.UP)).isAir()) {
            direction = Direction.DOWN;
            propertyPlaceCheck = properties.placement.canPlace(direction);
        }

        if (direction != Direction.UP && direction != Direction.DOWN) {
            rotation = 0;
        }

        float angle = DecorationItem.getVisualRotationYInDegrees(direction, rotation);

        if (player == null || !this.mayPlace(player, direction, itemStack, relativeBlockPos) || !propertyPlaceCheck) {
            return InteractionResult.FAIL;
        } else if (this.canPlaceAt(level, relativeBlockPos, angle) && itemStack.getItem() instanceof DecorationItem) {
            DecorationItem.place(itemStack, level, relativeBlockPos, direction, rotation);

            player.startUsingItem(useOnContext.getHand());
            itemStack.shrink(1);

            SoundEvent placeSound = properties.blockBase.defaultBlockState().getSoundType().getPlaceSound();
            level.playSound(null, blockPos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);

            return InteractionResult.SUCCESS_SERVER;
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

    public static void place(ItemStack itemStack, Level level, BlockPos blockPos, Direction direction, int rotation) {
        float angle = DecorationItem.getVisualRotationYInDegrees(direction, rotation);

        if (!(itemStack.getItem() instanceof  DecorationItem)) {
            Filament.LOGGER.error("Tried to place non-decoration item as decoration! Item: {}", BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        }

        DecorationData decorationData = ((DecorationItem)itemStack.getItem()).decorationData;
        if (decorationData.hasBlocks() && decorationData.countBlocks() == 0)
            Filament.LOGGER.warn("Found block data with potentially invalid blocks for {} while trying to place", decorationData.id());

        if (decorationData.hasBlocks()) {
            DecorationUtil.forEachRotated(decorationData.blocks(), blockPos, angle, blockPos2 -> {
                level.destroyBlock(blockPos2, false);

                BlockState blockState = DecorationRegistry.getDecorationBlock(decorationData.id()).defaultBlockState();

                if (decorationData.properties().mayBeLightSource()) {
                    blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission.getValue(blockState));
                }

                if (!decorationData.properties().waterloggable) {
                    blockState = blockState.setValue(DecorationBlock.WATERLOGGED, false);
                }
                else {
                    FluidState fluidState = level.getFluidState(blockPos2);
                    if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                        blockState = blockState.setValue(DecorationBlock.WATERLOGGED, true);
                    }
                }

                if (decorationData.isSimple()) {
                    blockState = blockState.setValue(SimpleDecorationBlock.ROTATION, (rotation + 4) % 8);
                }

                level.setBlockAndUpdate(blockPos2, blockState);

                if (!decorationData.isSimple() && level.getBlockEntity(blockPos2) instanceof DecorationBlockEntity decorationBlockEntity) {
                    decorationBlockEntity.setMain(new BlockPos(blockPos2).subtract(blockPos));
                    decorationBlockEntity.setItem(itemStack.copyWithCount(1));
                    decorationBlockEntity.setRotation(rotation);
                    decorationBlockEntity.setDirection(direction);
                    decorationBlockEntity.setupBehaviour(decorationData);
                    decorationBlockEntity.attach((ServerLevel) level);
                }
            });
        } else {
            BlockState blockState = DecorationRegistry.getDecorationBlock(decorationData.id()).defaultBlockState();

            if (!decorationData.properties().waterloggable) {
                blockState = blockState.setValue(DecorationBlock.WATERLOGGED, false);
            } else {
                FluidState fluidState = level.getFluidState(blockPos);
                if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                    blockState = blockState.setValue(DecorationBlock.WATERLOGGED, true);
                }
            }

            if (decorationData.properties().mayBeLightSource()) {
                blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission.getValue(blockState));
            }

            if (decorationData.isSimple()) {
                blockState = blockState.setValue(SimpleDecorationBlock.ROTATION, (rotation + 4) % 8);
            }

            level.setBlockAndUpdate(blockPos, blockState);
            if (!decorationData.isSimple() && level.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                decorationBlockEntity.setMain(BlockPos.ZERO);
                decorationBlockEntity.setItem(itemStack.copyWithCount(1));
                decorationBlockEntity.setRotation(rotation);
                decorationBlockEntity.setDirection(direction);
                decorationBlockEntity.setupBehaviour(decorationData);
                decorationBlockEntity.attach((ServerLevel) level);
            }
        }
    }
}
