package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationEntityBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@ApiStatus.Experimental
public class DecorationEntity extends Entity implements PolymerEntity, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();
    @Nullable
    private FilamentDecorationHolder decorationHolder;

    private Identifier decorationId;

    private ItemStack itemStack = ItemStack.EMPTY;
    private Direction direction = Direction.UP;
    private float visualRotation = 0.0f;
    private boolean destroyed = false;

    public DecorationEntity(EntityType<DecorationEntity> type, Level level) {
        super(type, level);
    }

    public void initFromItemStack(ItemStack stack, Direction facing, float rotation) {
        if (!(stack.getItem() instanceof DecorationItem decorationItem)) {
            Filament.LOGGER.error("Tried to create DecorationEntity from non-decoration item: {}, how did that even happen?", stack.getItem());
            return;
        }

        this.decorationId = decorationItem.getDecorationData().id();
        this.itemStack = stack.copy();
        this.itemStack.setCount(1);
        this.direction = facing;
        this.visualRotation = rotation;
        this.applyComponentsFromItemStack(stack);
        this.setupBehaviour(this.getDecorationData());
        this.refreshHolder();
    }

    private void setupBehaviour(@Nullable DecorationData data) {
        if (this.behaviours.isEmpty() && data != null) {
            this.initBehaviours(data.behaviour());
        }
    }

    private void refreshHolder() {
        if (this.level().isClientSide()) return;

        DecorationData data = this.getDecorationData();
        if (data == null) return;

        if (this.decorationHolder != null && this.decorationHolder.getAttachment() != null) {
            this.decorationHolder.getAttachment().destroy();
        }

        this.decorationHolder = this.createHolder();
        if (this.decorationHolder == null)
            return;

        decorationHolder.setYaw(this.getVisualRotation());

        new EntityAttachment(
                this.decorationHolder.asPolymerHolder(),
                this,
                this.decorationHolder.isAnimated()
        );

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                decorationBehaviour.onHolderAttach(this, this.decorationHolder);
            }
        }
    }

    @Nullable
    private FilamentDecorationHolder createHolder() {
        DecorationData data = this.getDecorationData();
        if (data == null) return null;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                FilamentDecorationHolder holder = decorationBehaviour.createHolder(this);
                if (holder != null) return holder;
            }
        }

        var decorationHolder1 = new DecorationHolder(this::getVisualItemStack);
        DecorationUtil.setupElements(decorationHolder1, this.getDecorationData(), this.direction, this.getVisualRotationYInDegrees(), this.getVisualItemStack(), this::interact);
        return decorationHolder1;
    }

    @Nullable
    public DecorationData getDecorationData() {
        return DecorationRegistry.getDecorationData(this.decorationId);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public @NonNull Direction getDirection() {
        return this.direction;
    }

    public float getVisualRotation() {
        return this.visualRotation;
    }

    private ItemStack getVisualItemStack() {
        DecorationData data = this.getDecorationData();
        if (data == null) return this.itemStack;
        ItemStack adjusted = DecorationUtil.placementAdjustedItem(
                this.itemStack, data.itemResource(),
                this.direction != Direction.DOWN && this.direction != Direction.UP,
                this.direction == Direction.DOWN
        );

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                adjusted = decorationBehaviour.visualItemStack(this, adjusted, null);
            }
        }

        return adjusted;
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        if (!CommonProtection.canInteractBlock(player.level(), BlockPos.containing(location), player.nameAndId(), player)) {
            return InteractionResult.FAIL;
        }
        DecorationData data = this.getDecorationData();
        if (data == null) return InteractionResult.FAIL;
        InteractionResult result = InteractionResult.PASS;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                result = decorationBehaviour.interact(serverPlayer, hand, location, this);
                if (result.consumesAction()) break;
            }
        }
        return result;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            this.destroyStructure(level, true);
            return true;
        }
        return false;
    }

    @Override
    public void kill(@NonNull ServerLevel level) {
        this.destroyStructure(level, true);
        super.kill(level);
    }

    public void destroyStructure(ServerLevel level, boolean dropItem) {
        if (this.destroyed) return;
        this.destroyed = true;
        DecorationData data = this.getDecorationData();
        if (data == null) return;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                if (dropItem) decorationBehaviour.modifyDrop(this, this.itemStack);
                decorationBehaviour.destroy(this, dropItem);
            }
        }
        level.playSound(null, this.blockPosition(), SoundEvents.ITEM_FRAME_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (data.properties().showBreakParticles()) {
            DecorationUtil.showBreakParticle(
                    level,
                    data.properties().useItemParticles ? this.itemStack : data.properties().blockBase().asItem().getDefaultInstance(),
                    (float) this.getX(), (float) this.getY(), (float) this.getZ()
            );
        }

        if (dropItem && data.properties().drops && !this.itemStack.isEmpty()) {
            Util.spawnAtLocation(this.level(), this.position(), this.itemStack.copy());
        }

        if (this.decorationHolder != null && this.decorationHolder.getAttachment() != null) {
            this.decorationHolder.getAttachment().destroy();
        }

        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.decorationHolder != null && this.decorationHolder.getAttachment() != null) {
            this.decorationHolder.tick();
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityTypes.BLOCK_DISPLAY;
    }

    @Override
    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    @Override
    public void initBehaviours(BehaviourConfigMap configMap) {
        BehaviourHolder.super.initBehaviours(configMap);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                decorationBehaviour.init(this);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        this.loadMain(input);
    }

    private void loadMain(ValueInput input) {
        input.read("Item", ItemStack.CODEC).ifPresent(this::applyComponentsFromItemStack);
        this.decorationId = input.read("DecorationId", Identifier.CODEC).orElse(null);
        this.direction = input.read("Direction", Direction.CODEC).orElse(Direction.UP);
        this.visualRotation = input.getFloatOr("VisualRotation", 0.0f);
        this.setupBehaviour(this.getDecorationData());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                decorationBehaviour.read(input, this);
            }
        }

        if (this.level() instanceof ServerLevel) {
            this.refreshHolder();
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        output.store("Item", ItemStack.CODEC, this.itemStack);
        if (this.decorationId != null) {
            output.store("DecorationId", Identifier.CODEC, this.decorationId);
        }
        output.store("Direction", Direction.CODEC, this.direction);
        output.putFloat("VisualRotation", this.visualRotation);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                decorationBehaviour.write(output, this);
            }
        }
    }

    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        this.setupBehaviour(this.getDecorationData());
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationEntityBehaviour<?> decorationBehaviour) {
                decorationBehaviour.applyImplicitComponents(this, componentGetter);
            }
        }
    }
}
