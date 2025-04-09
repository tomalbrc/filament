package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.mixin.accessor.HopperBlockEntityAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class Hopper implements BlockBehaviourWithEntity<Hopper.Config> {
    private final Config config;

    public Hopper(Config config) {
        this.config = config;
    }

    @Override
    public BlockEntityType<?> blockEntityType() {
        return BlockEntityType.HOPPER;
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace().getOpposite();
        return blockState.setValue(BlockStateProperties.FACING_HOPPER, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction).setValue(BlockStateProperties.ENABLED, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HopperBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : BlockBehaviourWithEntity.createTickerHelper(blockEntityType, BlockEntityType.HOPPER, Hopper::pushItemsTick);
    }

    public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity) {
        HopperBlockEntityAccessor accessor = ((HopperBlockEntityAccessor) hopperBlockEntity);
        accessor.setCooldownTime(accessor.cooldownTime() - 1);
        accessor.setTickedGameTime(level.getGameTime());
        if (!accessor.invokeIsOnCooldown()) {
            accessor.setCooldownTime(0);
            Config conf = get(hopperBlockEntity).config;
            tryMoveItems(level, blockPos, blockState, hopperBlockEntity, () -> suckInItems(level, hopperBlockEntity), conf);
        }
    }

    protected static void setChanged(Level level, BlockPos blockPos, BlockState blockState) {
        level.blockEntityChanged(blockPos);
        if (!blockState.isAir()) {
            level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
        }
    }

    private static void tryMoveItems(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity, BooleanSupplier booleanSupplier, Config config) {
        if (!level.isClientSide) {
            if (!((HopperBlockEntityAccessor) hopperBlockEntity).invokeIsOnCooldown() && blockState.getValue(HopperBlock.ENABLED)) {
                boolean changed = false;
                if (!hopperBlockEntity.isEmpty()) {
                    changed = HopperBlockEntityAccessor.invokeEjectItems(level, blockPos, hopperBlockEntity);
                }

                if (!((HopperBlockEntityAccessor) hopperBlockEntity).invokeInventoryFull()) {
                    changed |= booleanSupplier.getAsBoolean();
                }

                if (changed) {
                    ((HopperBlockEntityAccessor) hopperBlockEntity).setCooldownTime(config.cooldownTime);
                    setChanged(level, blockPos, blockState);
                }
            }
        }
    }

    public static boolean suckInItems(Level level, HopperBlockEntity hopper) {
        var conf = get(hopper).config;

        BlockPos blockPos = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY() + 1.0, hopper.getLevelZ());
        BlockState blockState = level.getBlockState(blockPos);
        Container container = HopperBlockEntity.getContainerAt(level, blockPos);
        if (container != null && conf.takeFromContainer) {
            Direction direction = Direction.DOWN;
            for (int i : HopperBlockEntityAccessor.invokeGetSlots(container, direction)) {
                if (tryTakeInItemFromSlot(hopper, container, i, direction)) {
                    return true;
                }
            }
        } else if (container == null && conf.pickupItemEntities) {
            boolean fullBlockShape = blockState.isCollisionShapeFullBlock(level, blockPos) && !blockState.is(BlockTags.DOES_NOT_BLOCK_HOPPERS);
            if (!fullBlockShape) {
                for (ItemEntity itemEntity : HopperBlockEntity.getItemsAtAndAbove(level, hopper)) {
                    if (HopperBlockEntity.addItem(hopper, itemEntity)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean tryTakeInItemFromSlot(HopperBlockEntity hopper, Container container, int i, Direction direction) {
        ItemStack itemStack = container.getItem(i);
        if (!itemStack.isEmpty() && canTakeItemFromContainer(hopper, container, itemStack, i, direction)) {
            int count = itemStack.getCount();
            ItemStack itemStack2 = HopperBlockEntity.addItem(container, hopper, container.removeItem(i, 1), null);
            if (itemStack2.isEmpty()) {
                container.setChanged();
                return true;
            }

            itemStack.setCount(count);
            if (count == 1) {
                container.setItem(i, itemStack);
            }
        }

        return false;
    }

    private static boolean canTakeItemFromContainer(HopperBlockEntity hopperContainer, Container container2, ItemStack itemStack, int i, Direction direction) {
        var self = get(hopperContainer);

        if (!container2.canTakeItem(hopperContainer, i, itemStack) || !self.canTake(itemStack)) {
            return false;
        } else {
            return !(container2 instanceof WorldlyContainer worldlyContainer) || worldlyContainer.canTakeItemThroughFace(i, itemStack, direction);
        }
    }

    private static Hopper get(HopperBlockEntity hopperBlockEntity) {
        return ((SimpleBlock) hopperBlockEntity.getBlockState().getBlock()).get(Behaviours.HOPPER);
    }

    public boolean canTake(ItemStack itemStack) {
        if (config.itemFilter == null) {
            config.itemFilter = new ObjectArrayList<>();
            config.itemTagFilter = new ObjectArrayList<>();
            if (config.filterItems != null) {
                for (String string : config.filterItems) {
                    if (string.startsWith("#")) {
                        config.itemTagFilter.add(TagKey.create(Registries.ITEM, ResourceLocation.parse(string.substring(1))));
                    } else {
                        config.itemFilter.add(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(string)));
                    }
                }
            }
        }

        for (Item item : config.itemFilter) {
            if (itemStack.is(item)) return true;
        }
        for (TagKey<Item> tagKey : config.itemTagFilter) {
            if (itemStack.is(tagKey)) return true;
        }

        return config.itemTagFilter.isEmpty() && config.itemFilter.isEmpty();
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState2.is(blockState.getBlock())) {
            this.checkPoweredState(level, blockPos, blockState);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(blockPos) instanceof HopperBlockEntity hopperBlockEntity) {
                player.openMenu(hopperBlockEntity);
                player.awardStat(Stats.INSPECT_HOPPER);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        this.checkPoweredState(level, blockPos, blockState);
    }

    private void checkPoweredState(Level level, BlockPos blockPos, BlockState blockState) {
        boolean bl = !level.hasNeighborSignal(blockPos);
        if (bl != blockState.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(blockPos, blockState.setValue(BlockStateProperties.ENABLED, bl), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    public Optional<Boolean> hasAnalogOutputSignal(BlockState blockState) {
        return Optional.of(true);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.FACING_HOPPER, rotation.rotate(blockState.getValue(BlockStateProperties.FACING_HOPPER)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.FACING_HOPPER)));
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING_HOPPER, BlockStateProperties.ENABLED);
        return true;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof HopperBlockEntity) {
            HopperBlockEntity.entityInside(level, blockPos, blockState, entity, (HopperBlockEntity) blockEntity);
        }
    }

    @Override
    public Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return Optional.of(false);
    }

    @Override
    @NotNull
    public Hopper.Config getConfig() {
        return this.config;
    }

    public static class Config {
        public List<String> filterItems;
        public boolean pickupItemEntities = true;
        public boolean takeFromContainer = true;
        public int cooldownTime = 8;

        transient public List<Item> itemFilter;
        transient public List<TagKey<Item>> itemTagFilter;
    }
}