package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.OxidizableRegistry;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.FilamentConfig;
import de.tomalbrc.filament.util.Util;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DecorationBlockEntity extends AbstractDecorationBlockEntity implements BlockEntityWithElementHolder, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();
    private Boolean replaceable;

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
    public void loadAdditional(CompoundTag input, HolderLookup.Provider lookup) {
        super.loadAdditional(input, lookup);

        if (this.isMain() || this.main == null) this.loadMain(input, lookup);
    }

    public void loadMain(CompoundTag input, HolderLookup.Provider lookup) {
        DecorationData decorationData = this.getDecorationData();
        if (decorationData == null) {
            Filament.LOGGER.error("No decoration data for {}!", this.getItem().getItem().getDescriptionId());
        }

        if (input.contains(ITEM)) ItemStack.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, lookup), input.get(ITEM)).ifSuccess(r -> {
            var item = r.getFirst();
            applyComponentsFromItemStack(item);
        });

        this.setupBehaviour(getDecorationData());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.read(input, Filament.SERVER.registryAccess(), this);
            }
        }
    }


    @Override
    public void saveAdditional(CompoundTag output, HolderLookup.Provider lookup) {
        super.saveAdditional(output, lookup);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> decorationBehaviour)
                decorationBehaviour.write(output, lookup, this);
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
            DecorationUtil.setupElements(holder, this.getDecorationData(), this.direction, this.getVisualRotationYInDegrees(), this.visualItemStack(getBlockState()), (this::interact));
        }

        this.decorationHolder = holder;

        return this.decorationHolder;
    }

    @Override
    public void attach(LevelChunk chunk) {
        if (main == null) main = BlockPos.ZERO;

        if (this.isMain()) {
            setupBehaviour(getDecorationData());

            FilamentDecorationHolder holder = this.getOrCreateHolder();
            if (holder != null && holder.getAttachment() == null) {
                var ignore = new BlockBoundAttachment(holder.asPolymerHolder(), chunk, this.getBlockState(), this.getBlockPos(), this.getBlockPos().getCenter(), holder.isAnimated());            }

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
        var adventureCheck = (FilamentConfig.getInstance().preventAdventureModeDecorationInteraction && !this.getDecorationData().properties().allowAdventureMode) && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
        if (adventureCheck || !CommonProtection.canInteractBlock(player.level(), BlockPos.containing(location), player.getGameProfile(), player))
            return InteractionResult.FAIL;

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

    public ItemStack visualItemStack(BlockState blockState) {
        var adjusted = DecorationUtil.placementAdjustedItem(this.getItem(), this.getDecorationData().itemResource(), this.direction != Direction.DOWN && this.direction != Direction.UP, this.direction == Direction.DOWN);
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                adjusted = decorationBehaviour.visualItemStack(this, adjusted, blockState);
            }
        }

        return adjusted;
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
                        this.getLevel().removeBlockEntity(blockPos);
                    }
                });
            } else {
                assert this.level != null;

                BlockPos blockPos = this.getBlockPos();

                if (data.properties().showBreakParticles)
                    DecorationUtil.showBreakParticle((ServerLevel) this.level, this.getDecorationData().properties().useItemParticles ? particleItem : this.getDecorationData().properties().blockBase.asItem().getDefaultInstance(), (float) blockPos.getCenter().x(), (float) blockPos.getCenter().y(), (float) blockPos.getCenter().z());

                BlockUtil.playBreakSound(this.level, this.getBlockPos(), this.getBlockState());
                this.level.destroyBlock(this.getBlockPos(), true);
                this.level.removeBlockEntity(this.getBlockPos());
            }
        }
    }

    @Override
    public void destroyStructure(boolean dropItem) {
        var visualStack = getBlock().visualItemStack(this.level, this.getBlockPos(), this.getBlockState());

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
                thisItemStack.applyComponents(this.components());
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

    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
            }
        }

        return blockState;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);

        setupBehaviour(getDecorationData());

        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.applyImplicitComponents(this, dataComponentGetter);
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.collectImplicitComponents(this, builder);
            }
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag valueOutput) {
        super.removeComponentsFromTag(valueOutput);

        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.behaviours) {
            if (behaviour.getValue() instanceof DecorationBehaviour<?> decorationBehaviour) {
                decorationBehaviour.removeComponentsFromTag(this, valueOutput, Filament.SERVER.registryAccess());
            }
        }
    }

    public boolean replaceable() {
        if (replaceable == null) {
            replaceable = has(Behaviours.OXIDIZABLE) || has(Behaviours.STRIPPABLE) || OxidizableRegistry.hasPrevious(getBlockState().getBlock());
        }

        return replaceable;
    }
}
