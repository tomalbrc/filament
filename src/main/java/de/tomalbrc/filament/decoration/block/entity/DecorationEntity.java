package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.decoration.util.ShulkerCollisionElement;
import de.tomalbrc.filament.decoration.util.VirtualCollisionTracker;
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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@ApiStatus.Experimental
public class DecorationEntity extends Entity implements DecorationLike, PolymerEntity, BehaviourHolder {
    private final BehaviourMap behaviours = new BehaviourMap();

    @Nullable
    private FilamentDecorationHolder decorationHolder;
    @Nullable
    private Identifier decorationId;

    private ItemStack itemStack = ItemStack.EMPTY;
    private Direction direction = Direction.UP;
    private float visualRotation = 0.0f;
    private boolean destroyed = false;

    @Nullable
    private DataComponentMap components;

    public DecorationEntity(EntityType<DecorationEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void initFromItemStack(ItemStack stack, Vec3 position, Direction placedFace, float yaw) {
        if (!(stack.getItem() instanceof DecorationItem decorationItem)) {
            Filament.LOGGER.error("Tried to create DecorationEntity from non-decoration item: {}", stack.getItem());
            return;
        }

        this.setPos(position.x, position.y, position.z);
        this.setYRot(yaw);
        this.setXRot(0.0f);
        this.visualRotation = yaw;

        this.decorationId = decorationItem.getDecorationData().id();
        this.itemStack = stack.copy();
        this.itemStack.setCount(1);
        this.direction = placedFace;
        this.applyComponentsFromItemStack(stack);
        this.setupBehaviour(this.getDecorationData());
        this.refreshHolder();
    }

    private void setupBehaviour(@Nullable DecorationData data) {
        if (this.behaviours.isEmpty() && data != null) {
            this.initBehaviours(data.behaviour());
        }
    }

    @Override
    public void initBehaviours(BehaviourConfigMap configMap) {
        DecorationLike.super.initBehaviours(configMap);
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.init(this);
            }
        }
    }

    private void refreshHolder() {
        if (this.level().isClientSide()) return;

        DecorationData data = this.getDecorationData();
        if (data == null) return;

        if (this.decorationHolder != null && this.decorationHolder.getAttachment() != null) {
            this.teardownCollision(this.decorationHolder);
            this.decorationHolder.getAttachment().destroy();
        }

        this.decorationHolder = this.createHolder();
        if (this.decorationHolder == null) return;

        this.decorationHolder.setYaw(this.getVisualRotationYInDegrees());

        new EntityAttachment(
                this.decorationHolder.asPolymerHolder(),
                this,
                this.decorationHolder.isAnimated()
        );

        this.setupCollision(this.decorationHolder);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.onHolderAttach(this, this.decorationHolder);
            }
        }
    }

    private void setupCollision(FilamentDecorationHolder holder) {
        DecorationData data = this.getDecorationData();
        if (data == null) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        float yawRad = this.visualRotation * Mth.DEG_TO_RAD;

        var blocks = data.blocks();
        var decoSize = data.size();
        if (blocks != null && !blocks.isEmpty()) {
            for (DecorationData.BlockConfig config : blocks) {
                this.spawnShulkerVolume(holder, config, yawRad, serverLevel);
            }
        } else if (decoSize != null) {
            Vector3f size = new Vector3f(decoSize.x(), decoSize.y(), decoSize.x());
            Vector3f origin = new Vector3f(0, 0, 0);
            this.spawnShulkerVolume(holder, new DecorationData.BlockConfig(origin, size), yawRad, serverLevel);
        }
    }

    private void spawnShulkerVolume(FilamentDecorationHolder holder, DecorationData.BlockConfig config,
                                    float yawRad, ServerLevel level) {
        Vector3f origin = config.origin();
        Vector3f size = config.size();

        int sx = Math.max(1, Math.round(size.x()));
        int sy = Math.max(1, Math.round(size.y()));
        int sz = Math.max(1, Math.round(size.z()));

        Vec3 base = this.position();

        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    Vector3f local = new Vector3f(
                            origin.x + x + 0.5f,
                            origin.y + y,
                            origin.z + z + 0.5f
                    );
                    local.rotateY(yawRad);

                    Vec3 world = base.add(local.x, local.y, local.z);

                    var element = new ShulkerCollisionElement();
                    element.setOffset(world.subtract(base));
                    holder.addElement(element);
                    VirtualCollisionTracker.register(element, level, world);
                }
            }
        }
    }

    private void teardownCollision(@Nullable FilamentDecorationHolder holder) {
        if (holder == null) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        for (var element : holder.asPolymerHolder().getElements()) {
            if (element instanceof ShulkerCollisionElement shulker) {
                Vec3 world = shulker.worldPosition();
                if (world != null) {
                    VirtualCollisionTracker.unregister(shulker, serverLevel, world);
                }
            }
        }
    }

    @Nullable
    private FilamentDecorationHolder createHolder() {
        DecorationData data = this.getDecorationData();
        if (data == null) return null;

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                FilamentDecorationHolder holder = behaviour.createHolder(this);
                if (holder != null) return holder;
            }
        }

        DecorationHolder holder = new DecorationHolder(this::getItemStack);
        DecorationUtil.setupElements(
                holder,
                this.getDecorationData(),
                this.direction,
                this.getVisualRotationYInDegrees(),
                this.getVisualItemStack(),
                this::interact
        );
        return holder;
    }

    @Override
    public Vec3 getDecorationPosition() {
        return this.position();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.visualRotation;
    }

    @Override
    public @NonNull Direction getDirection() {
        return this.direction;
    }

    @Override
    public FilamentDecorationHolder getOrCreateHolder() {
        return this.decorationHolder;
    }

    @Override
    public ItemStack getItem() {
        return this.itemStack;
    }

    @Override
    public Level getLevel() {
        return this.level();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public DecorationData getDecorationData() {
        return this.decorationId == null ? null : DecorationRegistry.getDecorationData(this.decorationId);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    private ItemStack getVisualItemStack() {
        DecorationData data = this.getDecorationData();
        if (data == null) return this.itemStack;

        ItemStack adjusted = DecorationUtil.placementAdjustedItem(
                this.itemStack,
                data.itemResource(),
                this.direction != Direction.DOWN && this.direction != Direction.UP,
                this.direction == Direction.DOWN
        );

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                adjusted = behaviour.visualItemStack(this, adjusted, null);
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

        if (this.getDecorationData() == null) return InteractionResult.FAIL;

        InteractionResult result = InteractionResult.PASS;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                result = behaviour.interact(serverPlayer, hand, location, this);
                if (result.consumesAction()) break;
            }
        }
        return result;
    }

    @Override
    public DataComponentMap components() {
        return this.components;
    }

    private void rebuildComponents() {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.collectImplicitComponents(this, builder);
            }
        }
        this.components = builder.build();
    }

    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);

        this.setupBehaviour(this.getDecorationData());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.applyImplicitComponents(this, componentGetter);
            }
        }

        this.rebuildComponents();
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            this.destroyStructure(level, true, source.getEntity() instanceof Player p ? p : null);
            return true;
        }
        return false;
    }

    @Override
    public void kill(@NonNull ServerLevel level) {
        this.destroyStructure(level, true, null);
        this.remove(Entity.RemovalReason.KILLED);
    }

    public void destroyStructure(ServerLevel level, boolean dropItem, @Nullable Player breaker) {
        if (this.destroyed) return;
        this.destroyed = true;

        DecorationData data = this.getDecorationData();
        if (data == null) return;

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                if (dropItem)
                    behaviour.modifyDrop(this, this.itemStack);

                behaviour.destroy(this, dropItem);
            }
        }

        level.playSound(null, this.blockPosition(), SoundEvents.ITEM_FRAME_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (data.properties().showBreakParticles()) {
            DecorationUtil.showBreakParticles(
                    level,
                    this.itemStack,
                    this.position()
            );
        }

        if (dropItem && data.properties().drops && !this.itemStack.isEmpty()) {
            ItemStack drop = this.itemStack.copy();
            if (this.components != null) {
                drop.applyComponents(this.components);
            }
            Util.spawnAtLocation(this.level(), this.position(), drop);
        }

        if (this.decorationHolder != null) {
            this.teardownCollision(this.decorationHolder);
            if (this.decorationHolder.getAttachment() != null) {
                this.decorationHolder.getAttachment().destroy();
            }
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.postBreak(this, this.blockPosition(), breaker);
            }
        }

        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (this.decorationHolder != null && decorationHolder.isAnimated() && this.decorationHolder.getAttachment() != null) {
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
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        this.decorationId = DecorationRegistry.canonicalize(input.read("DecorationId", Identifier.CODEC).orElse(null));
        this.setupBehaviour(this.getDecorationData());

        input.read("Components", DataComponentMap.CODEC).ifPresent(map -> this.components = map);

        input.read("Item", ItemStack.CODEC).ifPresent(this::applyComponentsFromItemStack);

        this.direction = input.read("Direction", Direction.CODEC).orElse(Direction.UP);
        this.visualRotation = input.getFloatOr("VisualRotation", this.getYRot());
        this.setYRot(this.visualRotation);

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.read(input, this);
            }
        }

        if (this.level() instanceof ServerLevel) {
            this.refreshHolder();
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        this.rebuildComponents();

        output.store("Item", ItemStack.CODEC, this.itemStack);
        if (this.decorationId != null) {
            output.store("DecorationId", Identifier.CODEC, this.decorationId);
        }
        if (this.components != null) {
            output.store("Components", DataComponentMap.CODEC, this.components);
        }
        output.store("Direction", Direction.CODEC, this.direction);
        output.putFloat("VisualRotation", this.getVisualRotationYInDegrees());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : this.behaviours) {
            if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                behaviour.write(output, this);
            }
        }
    }

    @Override
    public void destroyStructure(boolean dropItem) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.destroyStructure(serverLevel, dropItem, null);
        }
    }
}