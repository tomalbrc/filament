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
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.FilamentConfig;
import de.tomalbrc.filament.util.Util;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();

    @Nullable
    private ElementHolder decorationHolder;

    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }
    public DecorationBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);

        if (this.isMain()) this.loadMain(compoundTag, provider);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (isMain() && level != null && this.decorationHolder == null) {
            if (this.behaviours.isEmpty() && this.getDecorationData().behaviourConfig() != null && !this.getDecorationData().behaviourConfig().isEmpty())
                this.initBehaviours(this.getDecorationData().behaviourConfig());
            this.getOrCreateHolder();
        }
    }

    public void loadMain(CompoundTag compoundTag, HolderLookup.Provider provider) {
        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.error("No decoration data for {}!", this.itemStack.getDescriptionId());
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
    public ElementHolder getOrCreateHolder() {
        if (this.decorationHolder != null)
            return this.decorationHolder;

        ElementHolder altHolder = null;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
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
    @Nullable
    public ElementHolder getDecorationHolder() {
        return this.decorationHolder;
    }

    public void setDecorationHolder(@Nullable ElementHolder holder) {
        this.decorationHolder = holder;
    }

    @Override
    public void attach(LevelChunk chunk) {
        if (this.isMain() && this.itemStack != null) {
            ElementHolder elementHolder = this.getOrCreateHolder();
            if (elementHolder.getAttachment() == null) {
                new ChunkAttachment(elementHolder, chunk, this.getBlockPos().getCenter(), !(this.getDecorationHolder() instanceof DecorationHolder));
            }
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviourEntry : this.behaviours) {
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
        if (this.isMain() && this.behaviours.isEmpty() && decorationData.behaviourConfig() != null) {
            this.initBehaviours(decorationData.behaviourConfig());
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
        var adventureCheck = (FilamentConfig.getInstance().preventAdventureModeDecorationInteraction && !this.getDecorationData().properties().allowAdventureMode) && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
        if (adventureCheck || !CommonProtection.canInteractBlock(player.level(), BlockPos.containing(location), player.getGameProfile(), player))
            return InteractionResult.FAIL;

        if (!this.isMain()) {
            return this.getMainBlockEntity().decorationInteract(player, interactionHand, location);
        }

        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.warn("Can't interact with decoration: Missing decoration data! Location: {}", location);
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

    @Override
    protected void destroyBlocks(ItemStack particleItem) {
        BlockState decorationBlockState = this.getBlockState();
        if (DecorationRegistry.isDecoration(decorationBlockState) && this.getDecorationData() != null) {
            DecorationData data = this.getDecorationData();
            if (data.hasBlocks()) {
                Util.forEachRotated(data.blocks(), this.getBlockPos(), this.getVisualRotationYInDegrees(), blockPos -> {
                    if (this.getLevel() != null && DecorationRegistry.isDecoration(this.getLevel().getBlockState(blockPos))) {
                        if (data.properties().showBreakParticles)
                            Util.showBreakParticle((ServerLevel) this.level, blockPos, data.properties().useItemParticles ? particleItem : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());
                        this.getLevel().removeBlock(blockPos, true);
                    }
                });
            } else {
                assert this.level != null;

                BlockPos blockPos = this.getBlockPos();

                if (data.properties().showBreakParticles)
                    Util.showBreakParticle((ServerLevel) this.level, blockPos, this.getDecorationData().properties().useItemParticles ? particleItem : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());

                SoundEvent breakSound = data.properties().blockBase.defaultBlockState().getSoundType().getBreakSound();
                this.level.playSound(null, this.getBlockPos(),  breakSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.level.destroyBlock(this.getBlockPos(), true);
            }
        }
    }

    @Override
    public void destroyStructure(boolean dropItem) {
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
        ItemStack particleItem = this.getItem();
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

            particleItem = thisItemStack.copyAndClear();
        }

        this.removeHolder(this.decorationHolder);

        this.destroyBlocks(particleItem);
    }

    private void removeHolder(ElementHolder holder) {
        if (holder != null && holder.getAttachment() != null)
            holder.getAttachment().destroy();
    }

    public DecorationData getDecorationData() {
        return ((DecorationBlock)this.getBlockState().getBlock()).getDecorationData();
    }
}
