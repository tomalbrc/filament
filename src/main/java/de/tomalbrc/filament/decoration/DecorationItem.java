package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviours.item.Cosmetic;
import de.tomalbrc.filament.behaviours.item.Fuel;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.FuelRegistry;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DecorationItem extends Item implements PolymerItem, Equipable {
    final private DecorationData decorationData;
    final private PolymerModelData modelData;

    public DecorationItem(DecorationData decorationData, Item.Properties properties) {
        super(properties);
        this.decorationData = decorationData;
        this.modelData = this.decorationData.requestModel();

        if (this.decorationData.isFuel()) {
            Fuel.FuelConfig fuel = this.decorationData.behaviourConfig().get(Constants.Behaviours.FUEL);
            FuelRegistry.add(this, fuel.value);
        }
    }

    public DecorationData getDecorationData() {
        return this.decorationData;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (this.decorationData.vanillaItem() == Items.LEATHER_HORSE_ARMOR) {
            list.add(Component.literal("§9Dyeable"));
        }

        if (this.decorationData.properties() != null) {
            this.decorationData.properties().appendHoverText(list);
        }

        if (itemStack.has(DataComponents.CONTAINER)) {
            Iterator<ItemStack> itemStackIterator = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems().iterator();
            int i = 0;
            int j = 0;
            while(itemStackIterator.hasNext()) {
                ItemStack itemStack2 = itemStackIterator.next();
                j++;
                if (i <= 4) {
                    i++;
                    list.add(Component.translatable("container.shulkerBox.itemCount", itemStack2.getHoverName(), itemStack2.getCount()));
                }
            }
            if (j - i > 0) {
                list.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
            }
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

    public static float getVisualRotationYInDegrees(Direction direction, int rotation) {
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't use decoration Item: Missing decoration formats!");
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
        } else if (this.canPlaceAt(level, relativeBlockPos, useOnContext.getHorizontalDirection().getOpposite(), useOnContext.getClickedFace()) && itemStack.getItem() instanceof DecorationItem) {
            if (decorationData.hasBlocks()) {
                int finalRotation = rotation;
                Util.forEachRotated(decorationData.blocks(), relativeBlockPos, this.getVisualRotationYInDegrees(direction, rotation), blockPos2 -> {
                    level.destroyBlock(blockPos2, true);

                    BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, useOnContext.getHand(), itemStack, new BlockHitResult(useOnContext.getClickLocation(), useOnContext.getClickedFace(), blockPos2, useOnContext.isInside()));
                    BlockState blockState = DecorationRegistry.getDecorationBlock(decorationData.id()).getStateForPlacement(blockPlaceContext);
                    if (decorationData.properties() != null && decorationData.properties().isLightSource()) {
                        blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission);
                    }

                    if (!decorationData.properties().waterloggable)
                        blockState = blockState.setValue(DecorationBlock.WATERLOGGED, false);

                    if (decorationData.isSimple()) {
                        blockState = blockState.setValue(SimpleDecorationBlock.FACING, direction);
                        blockState = blockState.setValue(SimpleDecorationBlock.ROTATION, (finalRotation + 4) % 8);
                    }

                    level.setBlockAndUpdate(blockPos2, blockState);

                    if (!decorationData.isSimple() && level.getBlockEntity(blockPos2) instanceof DecorationBlockEntity decorationBlockEntity) {
                        decorationBlockEntity.setMain(new BlockPos(blockPos2).subtract(relativeBlockPos));
                        decorationBlockEntity.setItem(itemStack.copyWithCount(1));
                        decorationBlockEntity.setRotation(finalRotation);
                        decorationBlockEntity.setDirection(direction);
                        decorationBlockEntity.setupBehaviour(decorationData);
                        decorationBlockEntity.attach((ServerLevel) level);
                    }
                });
            } else {
                BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, useOnContext.getHand(), itemStack, new BlockHitResult(useOnContext.getClickLocation(), useOnContext.getClickedFace(), useOnContext.getClickedPos(), useOnContext.isInside()));

                BlockState blockState = DecorationRegistry.getDecorationBlock(decorationData.id()).getStateForPlacement(blockPlaceContext);
                blockState = blockState.setValue(DecorationBlock.PASSTHROUGH, true);

                if (!decorationData.properties().waterloggable) {
                    blockState = blockState.setValue(DecorationBlock.WATERLOGGED, false);
                }

                if (decorationData.properties() != null && decorationData.properties().isLightSource()) {
                    blockState = blockState.setValue(DecorationBlock.LIGHT_LEVEL, decorationData.properties().lightEmission);
                }

                if (decorationData.isSimple()) {
                    blockState = blockState.setValue(SimpleDecorationBlock.FACING, direction);
                    blockState = blockState.setValue(SimpleDecorationBlock.ROTATION, (rotation + 4) % 8);
                }

                level.setBlockAndUpdate(relativeBlockPos, blockState);
                if (!decorationData.isSimple() && level.getBlockEntity(relativeBlockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                    decorationBlockEntity.setMain(BlockPos.ZERO);
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

        if (decorationData.hasBlocks()) {
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

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        var res = super.use(level, user, hand);

        if (res.getResult() != InteractionResult.CONSUME && this.decorationData.isCosmetic()) {
            res = this.swapWithEquipmentSlot(this, level, user, hand);
        }

        return res;
    }

    @Override
    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        boolean cosmetic = decorationData.isCosmetic();
        if (cosmetic) {
            Cosmetic.CosmeticConfig cosmetic1 = this.decorationData.behaviourConfig().get(Constants.Behaviours.COSMETIC);
            if (cosmetic1.slot != null)
                return cosmetic1.slot;
        }
        return EquipmentSlot.MAINHAND;
    }
}
