package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.AbstractHorizontalFacing;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.mixin.accessor.ChestBlockInvoker;
import de.tomalbrc.filament.registry.OxidizableRegistry;
import de.tomalbrc.filament.registry.StrippableRegistry;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.TextUtil;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

public class AnimatedChest extends AbstractHorizontalFacing<AnimatedChest.Config> implements BlockBehaviour<AnimatedChest.Config>, DecorationBehaviour<AnimatedChest.Config>, ContainerLike {
    private final Config config;

    public FilamentContainer container;
    public ResourceKey<LootTable> lootTable;
    public long lootTableSeed;

    BlockEntityType<DecorationBlockEntity> TYPE;

    public AnimatedChest(Config config) {
        super(null);
        this.config = config;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        var id = block.builtInRegistryHolder().key().location();
        TYPE = (BlockEntityType<DecorationBlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id);
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        DecorationBehaviour.super.init(blockEntity);

        TYPE = (BlockEntityType<DecorationBlockEntity>) blockEntity.getType();

        this.container = new FilamentContainer(blockEntity, config.size, config.purge);

        var item = blockEntity.getItem();
        if (item.has(DataComponents.CONTAINER)) {
            Objects.requireNonNull(blockEntity.getItem().get(DataComponents.CONTAINER)).copyInto(container.items);
        }

        if (config.openAnimation != null) {
            container.setOpenCallback(() -> blockEntity.getOrCreateHolder().playAnimation(config.openAnimation, 2));
        }
        if (config.closeAnimation != null) {
            container.setCloseCallback(() -> blockEntity.getOrCreateHolder().playAnimation(config.closeAnimation, 2));
        }
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        container.setValid(false);

        if (!config.canPickup)
            Containers.dropContents(decorationBlockEntity.getLevel(), decorationBlockEntity.getBlockPos(), container);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.CHEST_TYPE);
    }

    protected BlockState updateShapeSimple(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockState blockState2) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        if (canConnectTo(blockState, blockState2) && direction.getAxis().isHorizontal()) {
            ChestType chestType = blockState2.getValue(ChestBlock.TYPE);
            if (blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE && chestType != ChestType.SINGLE && blockState.getValue(ChestBlock.FACING) == blockState2.getValue(ChestBlock.FACING) && ChestBlock.getConnectedDirection(blockState2) == direction.getOpposite()) {
                return blockState.setValue(ChestBlock.TYPE, chestType.getOpposite());
            }
        } else if (ChestBlock.getConnectedDirection(blockState) == direction) {
            return blockState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
        }

        return blockState;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        var blockState3 = updateShapeSimple(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockState2);

        if (canConnectTo(blockState, blockState2) && !blockState3.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE) && ChestBlock.getConnectedDirection(blockState3) == direction) {
            return blockState2.getBlock().withPropertiesOf(blockState3);
        }

        return blockState3;
    }

    public static boolean canConnectTo(BlockState state1, BlockState state2) {
        return state1.is(state2.getBlock()) || OxidizableRegistry.sameOxidizable(state1.getBlock(), state2.getBlock()) || StrippableRegistry.get(state1.getBlock()) == state2.getBlock() || StrippableRegistry.get(state2.getBlock()) == state1.getBlock();
    }


    protected @NotNull BlockState getStateForPlacementSimple(BlockState state, BlockPlaceContext blockPlaceContext) {
        ChestType chestType = ChestType.SINGLE;
        Direction direction = blockPlaceContext.getHorizontalDirection().getOpposite();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = blockPlaceContext.isSecondaryUseActive();
        Direction direction2 = blockPlaceContext.getClickedFace();
        if (direction2.getAxis().isHorizontal() && bl) {
            Direction direction3 = this.candidatePartnerFacing(state, blockPlaceContext, direction2.getOpposite());
            if (direction3 != null && direction3.getAxis() != direction2.getAxis()) {
                direction = direction3;
                chestType = direction3.getCounterClockWise() == direction2.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }

        if (chestType == ChestType.SINGLE && !bl) {
            if (direction == this.candidatePartnerFacing(state, blockPlaceContext, direction.getClockWise())) {
                chestType = ChestType.LEFT;
            } else if (direction == this.candidatePartnerFacing(state, blockPlaceContext, direction.getCounterClockWise())) {
                chestType = ChestType.RIGHT;
            }
        }

        return state.setValue(ChestBlock.FACING, direction).setValue(ChestBlock.TYPE, chestType).setValue(ChestBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext blockPlaceContext) {
        var blockState = getStateForPlacementSimple(state, blockPlaceContext);

        if (((BehaviourHolder) state.getBlock()).has(Behaviours.OXIDIZABLE)) {
            BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(ChestBlock.getConnectedDirection(blockState)));
            if (!blockState2.isAir() && blockState2.getBlock() instanceof DecorationBlock && !blockState2.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
                BehaviourHolder placed = (BehaviourHolder) blockState.getBlock();
                BehaviourHolder other = (BehaviourHolder) blockState2.getBlock();
                BlockState maybeFinal = blockState;
                BlockState blockStateOther = blockState2;
                if (placed.has(Behaviours.STRIPPABLE) != other.has(Behaviours.STRIPPABLE)) {
                    blockStateOther = this.unwaxed(placed, blockState).orElse(blockState2);
                    maybeFinal = this.unwaxed(other, blockState2).orElse(maybeFinal);
                }

                Block block = maybeFinal.getBlock();
                if (placed.has(Behaviours.OXIDIZABLE) && other.has(Behaviours.OXIDIZABLE) && placed.getOrThrow(Behaviours.OXIDIZABLE).getConfig().weatherState.ordinal() <= other.getOrThrow(Behaviours.OXIDIZABLE).getConfig().weatherState.ordinal()) {
                    block = blockStateOther.getBlock();
                }

                return block.withPropertiesOf(blockState2);
            }
        }

        return blockState;
    }

    private Optional<BlockState> unwaxed(BehaviourHolder copperChestBlock, BlockState blockState) {
        if (!copperChestBlock.has(Behaviours.STRIPPABLE)) {
            return Optional.of(blockState);
        }

        return Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
    }

    @Nullable
    private Direction candidatePartnerFacing(BlockState state, BlockPlaceContext blockPlaceContext, Direction direction) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction));
        return blockState.is(state.getBlock()) && blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? blockState.getValue(ChestBlock.FACING) : null;
    }

    @Override
    public void write(ValueOutput output, DecorationBlockEntity decorationBlockEntity) {
        if (!container.trySaveLootTable(output))
            ContainerHelper.saveAllItems(output.child("Container"), this.container.items);
    }

    @Override
    public void read(ValueInput input, DecorationBlockEntity decorationBlockEntity) {
        if (!container.tryLoadLootTable(input))
            input.child("Container").ifPresent(x -> ContainerHelper.loadAllItems(x, container.items));
    }

    @Override
    public Optional<Boolean> hasAnalogOutputSignal(BlockState blockState) {
        return Optional.of(Boolean.TRUE);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(blockState, level, blockPos, config.ignoreBlock));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level instanceof ServerLevel serverLevel) {
            MenuProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);
            if (menuProvider != null) {
                player.openMenu(menuProvider);
                player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
                if (config.angerPiglins) PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return this.combine(blockState, level, blockPos, config.ignoreBlock).apply(MENU_PROVIDER_COMBINER).orElse(null);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return itemStack;
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentGetter dataComponentGetter) {
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.container.items);
        SeededContainerLoot seededContainerLoot = dataComponentGetter.get(DataComponents.CONTAINER_LOOT);
        if (seededContainerLoot != null) {
            this.lootTable = seededContainerLoot.lootTable();
            this.lootTableSeed = seededContainerLoot.seed();
        }
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.container.items));
        if (this.lootTable != null) {
            builder.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
        }
    }

    @Override
    public void removeComponentsFromTag(DecorationBlockEntity decorationBlockEntity, ValueOutput valueOutput) {
        valueOutput.discard("LootTable");
        valueOutput.discard("LootTableSeed");
    }

    @Override
    public void modifyDrop(DecorationBlockEntity blockEntity, ItemStack itemStack) {
        if (config.canPickup) {
            itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
        }
    }

    @Override
    public Component customName() {
        return container.getBlockEntity().components().get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public @Nullable Container container() {
        return getContainer(container.getBlockEntity().getBlockState(), container.getBlockEntity().getLevel(), container.getBlockEntity().getBlockPos(), config.ignoreBlock);
    }

    @Override
    public boolean showCustomName() {
        return config.showCustomName;
    }

    @Override
    public boolean hopperDropperSupport() {
        return config.hopperDropperSupport;
    }

    @Override
    public boolean canPickUp() {
        return config.canPickup;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
        lootTable = resourceKey;
    }

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        return lootTable;
    }

    @Override
    public void setLootTableSeed(long l) {
        lootTableSeed = l;
    }

    @Override
    public long getLootTableSeed() {
        return lootTableSeed;
    }

    public static class Config {
        /**
         * The name displayed in the container UI
         */
        public String name = "Chest";

        /**
         * The name displayed in the container UI for double chests
         */
        public String nameDouble = "Double Chest";

        /**
         * The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
         */
        public int size = 9;

        /**
         * Indicates whether the container's contents should be cleared when no player is viewing the inventory.
         */
        public boolean purge = false;

        /**
         * The name of the animation to play when the container is opened
         */
        public String openAnimation = null;

        /**
         * The name of the animation to play when the container is closed
         */
        public String closeAnimation = null;

        /**
         * Flag to indicate whether the container can be picked up like shulker boxes.
         */
        public boolean canPickup = false;

        /**
         * Support hopper and dropper blocks
         */
        public boolean hopperDropperSupport = true;

        /**
         * Show custom name from item stack
         */
        public boolean showCustomName = true;

        /**
         * Ignore blocking by redstone conductor and cats
         */
        public boolean ignoreBlock = false;

        public Direction blockDirection = Direction.UP;

        /**
         * Anger nearby piglins
         */
        public boolean angerPiglins = true;
    }

    public Container getContainer(BlockState blockState, Level level, BlockPos blockPos, boolean ignoreBlock) {
        return combine(blockState, level, blockPos, ignoreBlock).apply(CONTAINER_COMBINER).orElse(null);
    }

    public boolean isBlockedAt(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        Direction dir = config.blockDirection;
        if (dir.getAxis().isHorizontal()) {
            var localRot = dir.toYRot();
            var facing = blockState.getValue(ChestBlock.FACING).toYRot();
            dir = Direction.fromYRot(facing - localRot);
        }
        return levelAccessor.getBlockState(blockPos.relative(dir)).isRedstoneConductor(levelAccessor, blockPos) || (dir == Direction.UP && ChestBlockInvoker.isCatSittingOnChest(levelAccessor, blockPos));
    }

    public DoubleBlockCombiner.NeighborCombineResult<DecorationBlockEntity> combine(BlockState blockState, Level level, BlockPos blockPos, boolean ignoreBlock) {
        BiPredicate<LevelAccessor, BlockPos> biPredicate;
        if (ignoreBlock) {
            biPredicate = (levelAccessor, blockPosx) -> false;
        } else {
            biPredicate = (levelAccessor, pos) -> this.isBlockedAt(levelAccessor, pos, blockState);
        }

        return DoubleBlockCombiner.combineWithNeigbour(TYPE, ChestBlock::getBlockType, ChestBlock::getConnectedDirection, ChestBlock.FACING, blockState, level, blockPos, biPredicate);
    }

    public static DoubleBlockCombiner.Combiner<DecorationBlockEntity, Optional<Container>> CONTAINER_COMBINER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public @NotNull Optional<net.minecraft.world.Container> acceptDouble(DecorationBlockEntity container, DecorationBlockEntity container2) {
            var c1 = container.getOrThrow(Behaviours.ANIMATED_CHEST).container;
            var c2 = container2.getOrThrow(Behaviours.ANIMATED_CHEST).container;
            return Optional.of(new CompoundContainer(c1, c2) {
                @Override
                public @NotNull List<ContainerUser> getEntitiesWithContainerOpen() {
                    return container2.getOrThrow(Behaviours.ANIMATED_CHEST).container.getEntitiesWithContainerOpen();
                }
            });
        }

        @Override
        public @NotNull Optional<net.minecraft.world.Container> acceptSingle(DecorationBlockEntity container) {
            return Optional.of(container.getOrThrow(Behaviours.ANIMATED_CHEST).container);
        }

        @Override
        public @NotNull Optional<net.minecraft.world.Container> acceptNone() {
            return Optional.empty();
        }
    };

    public boolean canOpen(Player player) {
        return !player.isSpectator();
    }

    public static DoubleBlockCombiner.Combiner<DecorationBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public @NotNull Optional<MenuProvider> acceptDouble(final DecorationBlockEntity chestBlockEntity, final DecorationBlockEntity chestBlockEntity2) {
            var c1 = chestBlockEntity.getOrThrow(Behaviours.ANIMATED_CHEST);
            var c2 = chestBlockEntity2.getOrThrow(Behaviours.ANIMATED_CHEST);
            final Container container = new CompoundContainer(c1.container, c2.container) {
                @Override
                public @NotNull List<ContainerUser> getEntitiesWithContainerOpen() {
                    return c2.container.getEntitiesWithContainerOpen();
                }
            };

            return Optional.of(new MenuProvider() {
                @Override
                @Nullable
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    c1.container.unpackLootTable(inventory.player);
                    c2.container.unpackLootTable(inventory.player);

                    if (c1.canOpen(player) && c2.canOpen(player)) {
                        return Util.createMenu(container, id, inventory, player);
                    } else {
                        return null;
                    }
                }

                @Override
                public @NotNull Component getDisplayName() {
                    var cname = c1.customName();
                    return cname != null && c1.showCustomName() ? cname : TextUtil.formatText(c1.config.nameDouble);
                }
            });
        }

        @Override
        public @NotNull Optional<MenuProvider> acceptSingle(DecorationBlockEntity chestBlockEntity) {
            var c1 = chestBlockEntity.getOrThrow(Behaviours.ANIMATED_CHEST);
            FilamentContainer container = c1.container;

            var menuProvider = new MenuProvider() {
                @Override
                @Nullable
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    if (c1.canOpen(player)) {
                        return Util.createMenu(container, id, inventory, player);
                    } else {
                        return null;
                    }
                }

                @Override
                public @NotNull Component getDisplayName() {
                    var cname = c1.customName();
                    return cname != null && c1.showCustomName() ? cname : TextUtil.formatText(c1.config.name);
                }
            };

            return Optional.of(menuProvider);
        }

        @Override
        public @NotNull Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };
}