package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.behaviours.decoration.*;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.AnimatedHolder;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.decoration.util.FunctionalDecoration;
import de.tomalbrc.filament.decoration.util.impl.ContainerImpl;
import de.tomalbrc.filament.decoration.util.impl.LockImpl;
import de.tomalbrc.filament.registry.filament.DecorationRegistry;
import de.tomalbrc.filament.registry.filament.ModelRegistry;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, FunctionalDecoration {
    public static final String SHOWCASE_KEY = "Showcase";

    @Nullable
    public AnimatedHolder animatedHolder;

    @Nullable
    private DecorationHolder decorationHolder;


    public ContainerImpl containerImpl;

    LockImpl lockImpl;

    public DecorationBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);

        if (this.isMain()) this.loadMain(compoundTag, provider);
    }

    public void loadMain(CompoundTag compoundTag, HolderLookup.Provider provider) {
        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.error("No decoration formats for " + (this.itemStack.getDescriptionId() == null ? "(null)" : this.itemStack.getDescriptionId()) + "!");
        } else if (this.decorationHolder == null) {
            this.makeHolder();
            this.setupBehaviour(decorationData);
        }

        if (this.containerImpl != null) {
            this.containerImpl.read(compoundTag, provider);
        }

        if (compoundTag.contains(SHOWCASE_KEY) && this.decorationHolder != null) {
            CompoundTag showcaseTag = compoundTag.getCompound(SHOWCASE_KEY);

            for (int i = 0; i < this.decorationHolder.getShowcaseData().size(); i++) {
                Showcase showcase = this.decorationHolder.getShowcaseData().get(i);
                String key = ITEM + i;
                if (showcase != null && showcaseTag.contains(key)) {
                    this.decorationHolder.setShowcaseItemStack(showcase, ItemStack.parseOptional(provider, showcaseTag.getCompound(key)));
                }
            }
        }

        if (this.lockImpl != null) {
            this.lockImpl.read(compoundTag);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);

        if (this.containerImpl != null) {
            this.containerImpl.write(compoundTag, provider);
        }

        if (this.lockImpl != null) {
            this.lockImpl.write(compoundTag);
        }

        if (decorationHolder != null && decorationHolder.isShowcase()) {
            CompoundTag showcaseTag = new CompoundTag();

            for (int i = 0; i < decorationHolder.getShowcaseData().size(); i++) {
                Showcase showcase = decorationHolder.getShowcaseData().get(i);
                if (showcase != null && !decorationHolder.getShowcaseItemStack(showcase).isEmpty())
                    showcaseTag.put(ITEM + i, decorationHolder.getShowcaseItemStack(showcase).save(provider));
            }

            compoundTag.put(SHOWCASE_KEY, showcaseTag);
        }
    }

    @Override
    public ElementHolder makeHolder() {
        if (this.level != null && this.getDecorationData() != null && this.getDecorationData().hasAnimation() && this.animatedHolder == null) {
            Animation animation = this.getDecorationData().behaviour().get(Constants.Behaviours.ANIMATION);
            Model model = ModelRegistry.getModel(animation.model);
            if (model == null) {
                Filament.LOGGER.error("No Animated-Java model named '" + animation.model + "' was found!");
            } else {
                this.animatedHolder = new AnimatedHolder(this, model);
            }
        }
        else if (this.decorationHolder == null && this.animatedHolder == null) {
            this.decorationHolder = new DecorationHolder();
            this.decorationHolder.setBlockEntity(this);
            if (this.decorationHolder.getElements().get(0) instanceof ItemDisplayElement itemDisplayElement) {
                itemDisplayElement.setItem(this.itemStack);
            }
        }

        return this.animatedHolder != null ? this.animatedHolder : this.decorationHolder;
    }


    @Override
    public void attach(LevelChunk chunk) {
        if (this.isMain() && this.itemStack != null) {
            ElementHolder elementHolder = this.makeHolder();
            if (elementHolder.getAttachment() == null) {
                new BlockBoundAttachment(elementHolder, chunk, this.getBlockState(), this.getBlockPos(), this.getBlockPos().getCenter(), this.animatedHolder != null);
            }
        }

        if (this.animatedHolder != null)
            this.animatedHolder.setRotation(this.getVisualRotationYInDegrees());
    }

    @Override
    public void attach(ServerLevel level) {
        this.attach((LevelChunk)level.getChunk(this.getBlockPos()));
    }

    @Override
    public void setupBehaviour(DecorationData decorationData) {
        // When placed, decorationId is not yet set?
        if (this.isMain()) {
            assert decorationData.behaviour() != null;

            if (decorationData.isContainer()) {
                Container container = decorationData.behaviour().get(Constants.Behaviours.CONTAINER);
                this.setContainerData(container);
            }

            if (decorationData.isLock()) {
                Lock lock = decorationData.behaviour().get(Constants.Behaviours.LOCK);
                this.setLockData(lock);
            }

            if (this.decorationHolder == null && this.animatedHolder == null) {
                this.makeHolder();
            }

            if (decorationData.isSeat()) {
                List<Seat> seat = decorationData.behaviour().get(Constants.Behaviours.SEAT);
                this.setSeatData(seat);
            }

            if (decorationData.isShowcase()) {
                List<Showcase> showcase = decorationData.behaviour().get(Constants.Behaviours.SHOWCASE);
                this.setShowcaseData(showcase);
            }

            if (decorationData.hasAnimation()) {
                Animation animation = decorationData.behaviour().get(Constants.Behaviours.LOCK);
                this.setAnimationData(animation);
            }
        }

        if (this.getDecorationData().isContainer() && this.getItem().has(DataComponents.CONTAINER)) {
            Container container = this.getDecorationData().behaviour().get(Constants.Behaviours.CONTAINER);
            if (container.canPickup)
                Objects.requireNonNull(this.itemStack.get(DataComponents.CONTAINER)).copyInto(containerImpl.container().items);
        }
    }

    @Override
    public void setAnimationData(@NotNull Animation animationData) {
        if (animationData.model != null) {
            Model model = ModelRegistry.getModel(animationData.model);
            if (model == null) {
                Filament.LOGGER.error("No AnimatedJava model named '" + animationData.model + "' was found!");
            }
        }
    }

    @Override
    public void setSeatData(@NotNull List<Seat> seatData) {
        if (this.decorationHolder != null) {
            this.decorationHolder.setSeatData(seatData);
        }
    }

    @Override
    public void setShowcaseData(@NotNull List<Showcase> showcaseData) {
        if (this.decorationHolder != null) {
            this.decorationHolder.setShowcaseData(showcaseData);
        }
    }

    @Override
    public void setLockData(Lock lockData) {
        // dont recreate lock formats!
        if (this.lockImpl != null)
            return;

        this.lockImpl = new LockImpl(lockData);
    }

    @Override
    public void setContainerData(@NotNull Container containerData) {
        // dont recreate container formats!
        if (this.containerImpl != null) {
            return;
        }

        MenuType<?> menuType = ContainerImpl.getMenuType(containerData);

        // internal container formats with minecraft container
        this.containerImpl = new ContainerImpl(containerData.name, new FilamentContainer(containerData.size, containerData.purge), menuType, containerData.purge);
    }


    public InteractionResult interact(Player player, InteractionHand interactionHand, Vec3 location) {
        return this.decorationInteract((ServerPlayer) player, interactionHand, location);
    }

    public InteractionResult decorationInteract(ServerPlayer player, InteractionHand interactionHand, Vec3 location) {
        if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
            return InteractionResult.PASS;

        if (!this.isMain()) {
            return this.getMainBlockEntity().decorationInteract(player, interactionHand, location);
        }

        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't interact with decoration: Missing decoration formats! Location: " + location.toString());
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

        if (this.decorationHolder != null) {
            if (this.decorationHolder.isSeat() && player.getItemInHand(interactionHand).isEmpty() && !this.decorationHolder.isShowcase()) {
                Seat seat = this.decorationHolder.getClosestSeat(location);

                if (seat != null && !this.decorationHolder.hasSeatedPlayer(seat)) {
                    this.decorationHolder.seatPlayer(seat, player);
                }

                return InteractionResult.CONSUME;
            }

            if (this.decorationHolder.isShowcase() && !player.isSecondaryUseActive()) {
                Showcase showcase = this.decorationHolder.getClosestShowcase(location);
                ItemStack itemStack = player.getItemInHand(interactionHand);

                if (this.decorationHolder.canUseShowcaseItem(showcase, itemStack)) {
                    if (!player.getItemInHand(interactionHand).isEmpty()) {

                        if (this.decorationHolder.getShowcaseItemStack(showcase) != null) {
                            Util.spawnAtLocation(this.level, this.getBlockPos().getCenter(), this.decorationHolder.getShowcaseItemStack(showcase));
                        }

                        this.decorationHolder.setShowcaseItemStack(showcase, itemStack.copyWithCount(1));
                        itemStack.shrink(1);
                    } else if (this.decorationHolder.getShowcaseItemStack(showcase) != null) {
                        player.setItemInHand(interactionHand, this.decorationHolder.getShowcaseItemStack(showcase));
                        this.decorationHolder.setShowcaseItemStack(showcase, ItemStack.EMPTY);
                    }

                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void destroyBlocks() {
        BlockState decorationBlockState = this.getBlockState();
        if (DecorationRegistry.isDecoration(decorationBlockState) && this.getDecorationData() != null) {
            if (this.getDecorationData().hasBlocks()) {
                Util.forEachRotated(this.getDecorationData().blocks(), this.getBlockPos(), this.getVisualRotationYInDegrees(), blockPos -> {
                    if (DecorationRegistry.isDecoration(this.getLevel().getBlockState(blockPos))) {
                        this.getLevel().destroyBlock(blockPos, false);
                        this.getLevel().removeBlockEntity(blockPos);
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
            if (this.main != null && this.getLevel().getBlockEntity(this.getBlockPos().subtract(this.main)) instanceof DecorationBlockEntity mainBlockEntity) {
                mainBlockEntity.destroyStructure(dropItem);
            }
            return;
        }

        if (dropItem) {
            ItemStack itemStack = this.getItem();
            if (this.getDecorationData().isContainer()) {
                Container container = this.getDecorationData().behaviour().get(Constants.Behaviours.CONTAINER);
                if (container.canPickup)
                    itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.containerImpl.container().getItems()));
            }

            Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), itemStack);
        }

        if (this.containerImpl != null && this.getDecorationData().isContainer()) {
            Container container = this.getDecorationData().behaviour().get(Constants.Behaviours.CONTAINER);
            if (!container.canPickup) {
                this.containerImpl.container().setValid(false);
                for (ItemStack itemStack : this.containerImpl.container().items) {
                    if (itemStack.isEmpty()) continue;

                    Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), itemStack);
                }
            }
        }

        if (this.decorationHolder != null && this.decorationHolder.isShowcase()) {
            this.decorationHolder.getShowcaseData().forEach(showcase -> {
                ItemStack itemStack = this.decorationHolder.getShowcaseItemStack(showcase);
                if (itemStack != null && !itemStack.isEmpty()) {
                    Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), this.decorationHolder.getShowcaseItemStack(showcase));
                }
            });
        }

        this.removeHolder(this.decorationHolder);
        this.removeHolder(this.animatedHolder);

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
                BlockState blockState = this.getBlockState();

                if (DecorationRegistry.isDecoration(blockState)) {
                    blockState.setValue(DecorationBlock.PASSTHROUGH, !collisionStructure);
                }
            });
        }
    }

    public DecorationData getDecorationData() {
        return ((DecorationBlock)this.getBlockState().getBlock()).getDecorationData();
    }
}
