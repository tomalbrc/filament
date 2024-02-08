package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.config.behaviours.decoration.*;
import de.tomalbrc.filament.config.data.DecorationData;
import de.tomalbrc.filament.decoration.holder.AnimatedElementHolder;
import de.tomalbrc.filament.decoration.holder.DecorationElementHolder;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.decoration.util.FunctionalDecoration;
import de.tomalbrc.filament.decoration.util.impl.ContainerImpl;
import de.tomalbrc.filament.decoration.util.impl.LockImpl;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.model.AjModel;

import java.util.List;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, FunctionalDecoration {
    public static final String DECORATION_KEY = "Decoration";
    public static final String ITEM_KEY = "Item";

    public static final String SHOWCASE_KEY = "Showcase";

    @Nullable
    public AnimatedElementHolder animatedElementHolder;

    @Nullable
    private DecorationElementHolder decorationElementHolder;


    public ContainerImpl containerImpl;

    LockImpl lockImpl;

    private ResourceLocation decorationId;

    public DecorationBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        this.decorationId = new ResourceLocation(compoundTag.getString(DECORATION_KEY));

        if (this.isMain()) this.loadMain(compoundTag);
    }

    public void loadMain(CompoundTag compoundTag) {
        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.error("No decoration data for " + this.decorationId == null ? "(null)" : this.decorationId.toString() + "!");
        } else if (this.decorationElementHolder == null) {
            this.makeHolder();
            this.setupBehaviour(decorationData);
        }


        if (this.containerImpl != null) {
            this.containerImpl.read(compoundTag);
        }

        if (compoundTag.contains(SHOWCASE_KEY) && this.decorationElementHolder != null) {
            CompoundTag showcaseTag = compoundTag.getCompound(SHOWCASE_KEY);

            for (int i = 0; i < this.decorationElementHolder.getShowcaseData().size(); i++) {
                Showcase showcase = this.decorationElementHolder.getShowcaseData().get(i);
                String key = ITEM_KEY + i;
                if (showcase != null) {
                    this.decorationElementHolder.setShowcaseItemStack(showcase, ItemStack.of(showcaseTag.getCompound(key)));
                }
            }
        }

        if (this.lockImpl != null) {
            this.lockImpl.read(compoundTag);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (decorationId != null) {
            compoundTag.putString(DECORATION_KEY, decorationId.toString());
        }

        if (this.containerImpl != null) {
            this.containerImpl.write(compoundTag);
        }

        if (this.lockImpl != null) {
            this.lockImpl.write(compoundTag);
        }

        if (decorationElementHolder != null && decorationElementHolder.isShowcase()) {
            CompoundTag showcaseTag = new CompoundTag();

            for (int i = 0; i < decorationElementHolder.getShowcaseData().size(); i++) {
                Showcase showcase = decorationElementHolder.getShowcaseData().get(i);
                if (showcase != null && !decorationElementHolder.getShowcaseItemStack(showcase).isEmpty())
                    showcaseTag.put(ITEM_KEY + i, decorationElementHolder.getShowcaseItemStack(showcase).save(new CompoundTag()));
            }

            compoundTag.put(SHOWCASE_KEY, showcaseTag);
        }
    }

    @Override
    public ElementHolder makeHolder() {
        if (this.level != null && this.getDecorationData() != null && this.getDecorationData().hasAnimation() && this.animatedElementHolder == null) {
            AjModel model = DecorationRegistry.getModel(this.getDecorationData().behaviour().animation.model + ".json");
            if (model == null) {
                Filament.LOGGER.error("No Animated-Java model named '" + this.getDecorationData().behaviour().animation.model + "' was found!");
            } else {
                this.animatedElementHolder = new AnimatedElementHolder(this, model);

            }
        }
        else if (this.decorationElementHolder == null && this.animatedElementHolder == null) {
            this.decorationElementHolder = new DecorationElementHolder();
            this.decorationElementHolder.setBlockEntity(this);
        }

        return this.animatedElementHolder != null ? this.animatedElementHolder : this.decorationElementHolder;
    }


    @Override
    public void attach(LevelChunk chunk) {
        if (this.isMain()) {
            ElementHolder elementHolder = this.makeHolder();
            if (elementHolder.getAttachment() == null) {
                new BlockBoundAttachment(elementHolder, chunk, this.getBlockState(), this.getBlockPos(), this.getBlockPos().getCenter(), this.animatedElementHolder != null);
            }
        }
    }

    @Override
    public void attach(ServerLevel level) {
        this.attach((LevelChunk)level.getChunk(this.getBlockPos()));
    }

    @Override
    public void setupBehaviour(DecorationData decorationData) {
        this.decorationId = decorationData.id();

        // When placed, decorationId is not yet set?
        if (this.isMain()) {
            assert decorationData.behaviour() != null;

            if (decorationData.isContainer()) {
                this.setContainerData(decorationData.behaviour().container);
            }
            if (decorationData.isSeat()) {
                this.setSeatData(decorationData.behaviour().seat);
            }
            if (decorationData.isShowcase()) {
                this.setShowcaseData(decorationData.behaviour().showcase);
            }
            if (decorationData.isLock()) {
                this.setLockData(decorationData.behaviour().lock);
            }

            if (this.decorationElementHolder == null && this.animatedElementHolder == null) {
                this.makeHolder();
            }

            if (decorationData.hasAnimation()) {
                this.setAnimationData(decorationData.behaviour().animation);
            }
        }
    }

    @Override
    public void setAnimationData(@NotNull Animation animationData) {
        if (animationData.model != null) {
            AjModel model = DecorationRegistry.getModel(animationData.model + ".json");
            if (model == null) {
                Filament.LOGGER.error("No AnimatedJava model named '" + animationData.model + "' was found!");
            }
        }
    }

    @Override
    public void setSeatData(@NotNull List<Seat> seatData) {
        this.decorationElementHolder.setSeatData(seatData);
    }

    @Override
    public void setShowcaseData(@NotNull List<Showcase> showcaseData) {
        this.decorationElementHolder.setShowcaseData(showcaseData);
    }

    @Override
    public void setLockData(Lock lockData) {
        // dont recreate lock data!
        if (this.lockImpl != null)
            return;

        this.lockImpl = new LockImpl(lockData);
    }

    @Override
    public void setContainerData(@NotNull Container containerData) {
        // dont recreate container data!
        if (this.containerImpl != null) {
            return;
        }

        MenuType<?> menuType = ContainerImpl.getMenuType(containerData);

        // internal container data with minecraft container
        this.containerImpl = new ContainerImpl(containerData.name, new FilamentContainer(containerData.size, containerData.purge), menuType, containerData.purge);
    }


    public InteractionResult interact(Player player, InteractionHand interactionHand, Vec3 location) {
        return this.decorationInteract((ServerPlayer) player, interactionHand, location);
    }

    public InteractionResult decorationInteract(ServerPlayer player, InteractionHand interactionHand, Vec3 location) {
        if (!this.isMain()) {
            return this.getMainBlockEntity().decorationInteract(player, interactionHand, location);
        }

        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't interact with decoration: Missing decoration data! Location: " + location.toString());
            return InteractionResult.FAIL;
        }

        if (this.lockImpl != null && lockImpl.interact(player, this)) {
            return InteractionResult.CONSUME;
        }

        if (this.containerImpl != null && !player.isSecondaryUseActive()) {
            Component containerName = Component.literal(containerImpl.name() == null ? "filament container" : containerImpl.name());
            if (this.containerImpl.container().getContainerSize() % 9 == 0) {
                player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> new ChestMenu(containerImpl.menuType(), i, playerInventory, containerImpl.container(), containerImpl.container().getContainerSize() / 9), containerName));
            } else if (this.containerImpl.container().getContainerSize() == 5) {
                player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> new HopperMenu(i, playerInventory, containerImpl.container()), containerName));
            }

            return InteractionResult.CONSUME;
        }

        if (this.decorationElementHolder != null) {
            if (this.decorationElementHolder.isSeat() && player.getItemInHand(interactionHand).isEmpty() && !this.decorationElementHolder.isShowcase()) {
                Seat seat = this.decorationElementHolder.getClosestSeat(location);

                if (seat != null && !this.decorationElementHolder.hasSeatedPlayer(seat)) {
                    this.decorationElementHolder.seatPlayer(seat, player);
                }

                return InteractionResult.CONSUME;
            }

            if (this.decorationElementHolder.isShowcase() && !player.isSecondaryUseActive()) {
                Showcase showcase = this.decorationElementHolder.getClosestShowcase(location);
                ItemStack itemStack = player.getItemInHand(interactionHand);

                if (this.decorationElementHolder.canUseShowcaseItem(showcase, itemStack)) {
                    if (!player.getItemInHand(interactionHand).isEmpty()) {

                        if (this.decorationElementHolder.getShowcaseItemStack(showcase) != null) {
                            Util.spawnAtLocation(this.level, this.getBlockPos().getCenter(), this.decorationElementHolder.getShowcaseItemStack(showcase));
                        }

                        this.decorationElementHolder.setShowcaseItemStack(showcase, itemStack.copyWithCount(1));
                        itemStack.shrink(1);
                    } else if (this.decorationElementHolder.getShowcaseItemStack(showcase) != null) {
                        player.setItemInHand(interactionHand, this.decorationElementHolder.getShowcaseItemStack(showcase));
                        this.decorationElementHolder.setShowcaseItemStack(showcase, ItemStack.EMPTY);
                    }

                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public void setupBlockEntities() {
        boolean hasBlocksConfig = this.getDecorationData().blocks() != null;
        if (hasBlocksConfig && this.isMain()) {
            Util.forEachRotated(this.getDecorationData().blocks(), this.getBlockPos(), this.getVisualRotationYInDegrees(), blockPos -> {
                BlockState blockState = this.getLevel().getBlockState(blockPos);

                boolean fb = blockState.is(BlockRegistry.DECORATION_BLOCK);
                if (!fb && blockState.isAir()) { // Force replace air
                    this.getLevel().setBlockAndUpdate(blockPos, BlockRegistry.DECORATION_BLOCK.defaultBlockState());
                } else if (fb) {
                    DecorationBlockEntity decorationBlockEntity = (DecorationBlockEntity) this.getLevel().getBlockEntity(blockPos);
                    if (decorationBlockEntity != null) {
                        decorationBlockEntity.setMain(this.getBlockPos());
                        decorationBlockEntity.setupBehaviour(this.getDecorationData());
                    }
                }
            });
        }
    }

    @Override
    protected void destroyBlocks() {
        BlockState decorationBlockState = this.getLevel().getBlockState(this.getBlockPos());
        if (decorationBlockState.is(BlockRegistry.DECORATION_BLOCK) && this.getDecorationData() != null) {
            if (this.getDecorationData().blocks() != null) {
                Util.forEachRotated(this.getDecorationData().blocks(), this.main, this.getVisualRotationYInDegrees(), blockPos -> {
                    if (this.getLevel().getBlockState(blockPos).is(BlockRegistry.DECORATION_BLOCK)) {
                        this.getLevel().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                });
            } else {
                this.getLevel().destroyBlock(this.getBlockPos(), true);
            }
        }
    }

    @Override
    public void destroyStructure(boolean dropItem) {
        if (!this.isMain()) {
            if (this.getLevel().getBlockEntity(this.main) instanceof DecorationBlockEntity mainBlockEntity) {
                mainBlockEntity.destroyStructure(dropItem);
            }
            return;
        }

        if (dropItem) {
            Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), this.getItem());
        }

        if (this.containerImpl != null) {
            this.containerImpl.container().setValid(false);
            for (ItemStack itemStack : this.containerImpl.container().items) {
                if (itemStack.isEmpty()) continue;

                Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), itemStack);
            }
        }

        if (this.decorationElementHolder != null && this.decorationElementHolder.isShowcase()) {
            this.decorationElementHolder.getShowcaseData().forEach(showcase -> {
                ItemStack itemStack = this.decorationElementHolder.getShowcaseItemStack(showcase);
                if (itemStack != null && !itemStack.isEmpty()) {
                    Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), this.decorationElementHolder.getShowcaseItemStack(showcase));
                }
            });
        }

        this.removeHolder(this.decorationElementHolder);
        this.removeHolder(this.animatedElementHolder);

        this.destroyBlocks();
    }

    private void removeHolder(ElementHolder holder) {
        if (holder != null && holder.getAttachment() != null)
            holder.getAttachment().destroy();
    }

    @Override
    protected void setCollisionStructure(boolean collisionStructure) {
        if (this.getDecorationData() != null && this.getDecorationData().blocks() != null) {
            Util.forEachRotated(this.getDecorationData().blocks(), this.getBlockPos(), this.getVisualRotationYInDegrees(), blockPos -> {
                BlockState blockState = this.getLevel().getBlockState(blockPos);

                if (blockState.is(BlockRegistry.DECORATION_BLOCK)) {
                    blockState.setValue(DecorationBlock.PASSTHROUGH, !collisionStructure);
                }
            });
        }
    }

    public DecorationData getDecorationData() {
        return DecorationRegistry.getDecorationDefinition(decorationId);
    }
}
