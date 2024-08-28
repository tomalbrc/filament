package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.decoration.DecorationBehaviour;
import de.tomalbrc.filament.behaviours.BehaviourConfigMap;
import de.tomalbrc.filament.behaviours.BehaviourHolder;
import de.tomalbrc.filament.behaviours.BehaviourMap;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();

    @Nullable
    private ElementHolder decorationHolder;

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
            this.itemStack.getDescriptionId();
            Filament.LOGGER.error("No decoration formats for " + this.itemStack.getDescriptionId() + "!");
        } else if (this.decorationHolder == null) {
            this.getOrCreateHolder();
            this.setupBehaviour(decorationData);
        }

        for (Map.Entry<ResourceLocation, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.read(compoundTag, provider, this);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);

        for (Map.Entry<ResourceLocation,Behaviour<?>> behaviour : behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour)
                decorationBehaviour.write(compoundTag, provider, this);
        }
    }

    @Override
    public ElementHolder getOrCreateHolder() {
        if (this.decorationHolder != null)
            return this.decorationHolder;

        ElementHolder altHolder = null;
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviourEntry : this.behaviours) {
            if (behaviourEntry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                altHolder = decorationBehaviour.createHolder(this);
                if (altHolder != null) {
                    break;
                }
            }
        }

        if (altHolder == null)
            altHolder = new DecorationHolder(this);

        this.decorationHolder = altHolder;

        return this.decorationHolder;
    }

    @Override
    public ElementHolder getDecorationHolder() {
        return this.decorationHolder;
    }

    public void setDecorationHolder(ElementHolder holder) {
        this.decorationHolder = holder;
    }

    @Override
    public void attach(LevelChunk chunk) {
        if (this.isMain() && this.itemStack != null) {
            ElementHolder elementHolder = this.getOrCreateHolder();
            if (elementHolder.getAttachment() == null) {
                new BlockBoundAttachment(elementHolder, chunk, this.getBlockState(), this.getBlockPos(), this.getBlockPos().getCenter(), !(this.getDecorationHolder() instanceof DecorationHolder));
            }
        }

        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviourEntry : this.behaviours) {
            if (behaviourEntry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.onElementAttach(this, this.decorationHolder);
            }
        }
    }

    @Override
    public void attach(ServerLevel level) {
        this.attach((LevelChunk)level.getChunk(this.getBlockPos()));
    }

    public void setupBehaviour(DecorationData decorationData) {
        // When placed, decorationId is not yet set?
        if (this.isMain()) {
            assert decorationData.behaviourConfig() != null;

            if (this.decorationHolder == null) {
                this.getOrCreateHolder();
            }

            this.initBehaviours(decorationData.behaviourConfig());
        }
    }

    public void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        this.behaviours.from(behaviourConfigMap);
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.init(this);
            }
        }
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

        InteractionResult res = InteractionResult.PASS;
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                res = decorationBehaviour.interact(player, interactionHand, location, this);
                if (res.consumesAction())
                    break;
            }
        }

        return res;
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

        ItemStack thisItemStack = this.getItem();
        for (Map.Entry<ResourceLocation, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                if (dropItem)
                    decorationBehaviour.modifyDrop(this, thisItemStack);
                decorationBehaviour.destroy(this, dropItem);
            }
        }

        if (dropItem) {
            Util.spawnAtLocation(this.getLevel(), this.getBlockPos().getCenter(), thisItemStack);
        }

        this.removeHolder(this.decorationHolder);

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

    @Override
    public <T extends Behaviour> T getBehaviour(ResourceLocation resourceLocation) {
        return this.behaviours.get(resourceLocation);
    }
}
