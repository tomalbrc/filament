package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.*;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();

    @Nullable
    private FilamentDecorationHolder decorationHolder;

    public DecorationBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);

        if (this.isMain()) this.loadMain(compoundTag, provider);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level != null && version == 1) CompletableFuture.runAsync(()->{
            if (level.getServer() != null) level.getServer().execute(() -> {
                UpgradeUtil.upgradeDecoration1to2(this, this.level, this.getBlockPos());
                this.version = 2;
            });
        });

        if (isMain() && level != null && this.decorationHolder == null) {
            this.getOrCreateHolder();
        }
    }

    public void loadMain(CompoundTag compoundTag, HolderLookup.Provider provider) {
        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.error("No decoration data for {}!", this.itemStack.getItem().getDescriptionId());
        } else if (this.decorationHolder == null) {
            this.setupBehaviour(decorationData);
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.read(compoundTag, provider, this);
            }
        }
    }


    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour)
                decorationBehaviour.write(compoundTag, provider, this);
        }
    }

    @Override
    public FilamentDecorationHolder getOrCreateHolder() {
        if (this.decorationHolder != null)
            return this.decorationHolder;

        FilamentDecorationHolder holder = null;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                holder = decorationBehaviour.createHolder(this);
                if (holder != null) {
                    break;
                }
            }
        }

        if (holder == null) {
            holder = new DecorationHolder(this::getItem);
            DecorationUtil.setupElements(holder, this.getDecorationData(), this.direction, this.getVisualRotationYInDegrees(), this.visualItemStack(), (this::interact));
        }

        this.decorationHolder = holder;

        return this.decorationHolder;
    }

    public void updateModel() {
        this.getOrCreateHolder().updateVisualItem(this.visualItemStack());
    }

    @Override
    public void attach(LevelChunk chunk) {
        if (this.isMain() && this.itemStack != null) {
            FilamentDecorationHolder holder = this.getOrCreateHolder();
            if (holder.getAttachment() == null) {
                var ignore = new BlockBoundAttachment(holder.asPolymerHolder(), chunk, this.getBlockState(), this.getBlockPos(), this.getBlockPos().getCenter(), holder.isAnimated());
            }

            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviourEntry : this.behaviours) {
                if (behaviourEntry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                    decorationBehaviour.onHolderAttach(this, this.decorationHolder);
                }
            }
        }
    }

    public void setupBehaviour(DecorationData decorationData) {
        // When placed, decorationId is not yet set?
        if (this.isMain() && this.behaviours.isEmpty()) {
            this.initBehaviours(decorationData.behaviour());
        }
    }

    @Override
    public void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        BehaviourHolder.super.initBehaviours(behaviourConfigMap);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.init(this);
            }
        }
    }

    public InteractionResult interact(Player player, InteractionHand interactionHand, Vec3 location) {
        return this.decorationInteract((ServerPlayer) player, interactionHand, location);
    }

    public InteractionResult decorationInteract(ServerPlayer player, InteractionHand interactionHand, Vec3 location) {
        if (FilamentConfig.getInstance().preventAdventureModeDecorationInteraction && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
            return InteractionResult.PASS;

        if (!this.isMain()) {
            return this.getMainBlockEntity().decorationInteract(player, interactionHand, location);
        }

        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't interact with decoration: Missing decoration data! Location: {}", location.toString());
            return InteractionResult.FAIL;
        }

        InteractionResult res = InteractionResult.PASS;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                res = decorationBehaviour.interact(player, interactionHand, location, this);
                if (res.consumesAction())
                    break;
            }
        }

        return res;
    }

    public ItemStack visualItemStack() {
        var adjusted = DecorationUtil.placementAdjustedItem(this.itemStack, this.getDecorationData().itemResource(), this.direction != Direction.DOWN && this.direction != Direction.UP, this.direction == Direction.DOWN);
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                ItemStack modifiedStack = decorationBehaviour.visualItemStack(this, adjusted);
                if (modifiedStack != null) {
                    return modifiedStack;
                }
            }
        }

        return DecorationUtil.placementAdjustedItem(this.itemStack, this.getDecorationData().itemResource(), this.direction != Direction.DOWN && this.direction != Direction.UP, this.direction == Direction.DOWN);
    }

    @Override
    protected void destroyBlocks(ItemStack particleItem) {
        BlockState decorationBlockState = this.getBlockState();
        if (DecorationRegistry.isDecoration(decorationBlockState) && this.getDecorationData() != null) {
            DecorationData data = this.getDecorationData();
            if (data.hasBlocks()) {
                DecorationUtil.forEachRotated(data.blocks(), this.getBlockPos(), this.getVisualRotationYInDegrees(), blockPos -> {
                    if (this.getLevel() != null && DecorationRegistry.isDecoration(this.getLevel().getBlockState(blockPos))) {
                        if (data.properties().showBreakParticles)
                            DecorationUtil.showBreakParticle((ServerLevel) this.level, data.properties().useItemParticles ? particleItem : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());
                        this.getLevel().destroyBlock(blockPos, false);
                    }
                });
            } else {
                assert this.level != null;

                BlockPos blockPos = this.getBlockPos();

                if (data.properties().showBreakParticles)
                    DecorationUtil.showBreakParticle((ServerLevel) this.level, this.getDecorationData().properties().useItemParticles ? particleItem : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());

                BlockUtil.playBreakSound(this.level, this.getBlockPos(), this.getBlockState());
                this.level.destroyBlock(this.getBlockPos(), true);
            }
        }
    }

    @Override
    public void destroyStructure(boolean dropItem) {
        var visualStack = this.visualItemStack();

        if (!this.isMain()) {
            if (this.getLevel() != null && this.main != null && this.getLevel().getBlockEntity(this.getBlockPos().subtract(this.main)) instanceof DecorationBlockEntity mainBlockEntity) {
                mainBlockEntity.destroyStructure(dropItem);
            }
            return;
        }

        if (!this.getDecorationData().properties().drops) {
            dropItem = false;
        }

        ItemStack thisItemStack = this.getItem();
        if (thisItemStack != null && !thisItemStack.isEmpty()) {
            for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.behaviours) {
                if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                    if (dropItem)
                        decorationBehaviour.modifyDrop(this, thisItemStack);
                    decorationBehaviour.destroy(this, dropItem);
                }
            }

            if (dropItem) {
                Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), thisItemStack.copy());
            }
        }

        this.removeHolder(this.decorationHolder);

        this.destroyBlocks(visualStack);
    }

    private void removeHolder(FilamentDecorationHolder holder) {
        if (holder != null && holder.getAttachment() != null)
            holder.getAttachment().destroy();
    }

    public DecorationBlock getBlock() {
        return (DecorationBlock) this.getBlockState().getBlock();
    }

    public DecorationData getDecorationData() {
        return this.getBlock().getDecorationData();
    }

    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.updateShape(this, blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
            }
        }

        return blockState;
    }

    public Direction getFacing() {
        return Direction.fromYRot(this.getVisualRotationYInDegrees());
    }
}
