package de.tomalbrc.filament.item;

import de.tomalbrc.filament.mixin.behaviour.trident.ThrownTridentAccessor;
import de.tomalbrc.filament.registry.EntityRegistry;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.accessors.DisplayAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ItemDisplayAccessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;

public class TridentEntity extends ThrownTrident implements PolymerEntity {
    protected final ElementHolder holder = new ElementHolder();
    protected final InteractionElement interactionElement = new InteractionElement();

    @Override
    public void setXRot(float x) {
        Vec3 delta = this.getDeltaMovement();
        if (delta.length() == 0) return;
        var xr = (float)(Mth.atan2(delta.y, delta.horizontalDistance()) * Mth.RAD_TO_DEG);
        super.setXRot(xr);
    }
    @Override
    public void setYRot(float y) {
        Vec3 delta = this.getDeltaMovement();
        if (delta.length() == 0) return;
        var yr = (float)(Mth.atan2(delta.x, -delta.z) * Mth.RAD_TO_DEG);
        super.setYRot(yr);
    }


    @Override
    public void setPickupItemStack(ItemStack stack) {
        super.setPickupItemStack(stack);
    }

    public TridentEntity(EntityType<? extends Entity> entityType, Level world) {
        super((EntityType<? extends ThrownTrident>) entityType, world);
        initHolder();
    }

    public TridentEntity(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        this(EntityRegistry.FILAMENT_TRIDENT, level);
        this.setPos(livingEntity.getEyePosition());
        this.setOwner(livingEntity);
        setPickupItemStack(itemStack);

        this.entityData.set(ThrownTridentAccessor.getID_LOYALTY(), this.getLoyaltyFromItem(itemStack));
        this.entityData.set(ThrownTridentAccessor.getID_FOIL(), itemStack.hasFoil());
    }

    private byte getLoyaltyFromItem(ItemStack itemStack) {
        Level var3 = this.level();
        if (var3 instanceof ServerLevel serverLevel) {
            return (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, itemStack, this), 0, 127);
        } else {
            return 0;
        }
    }

    private void initHolder() {
        EntityAttachment.of(this.holder, this);

        this.interactionElement.setSize(0.5f, 0.5f);
        this.interactionElement.ignorePositionUpdates();
        this.holder.addElement(this.interactionElement);
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement.getEntityId());
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(SynchedEntityData.DataValue.create(ItemDisplayAccessor.getDATA_ITEM_STACK_ID(), getPickupItem()));
        data.add(SynchedEntityData.DataValue.create(DisplayAccessor.getDATA_POS_ROT_INTERPOLATION_DURATION_ID(), 2));
        data.add(SynchedEntityData.DataValue.create(DisplayAccessor.getDATA_LEFT_ROTATION_ID(), new Quaternionf().rotateZ(Mth.HALF_PI).rotateX(-Mth.HALF_PI).rotateY(Mth.PI).normalize()));
        data.add(SynchedEntityData.DataValue.create(DisplayAccessor.getDATA_TRANSLATION_ID(), new Vector3f(0.f, -1.0f, 0.f).rotateX(-Mth.HALF_PI)));
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        PolymerEntity.super.onEntityPacketSent(consumer, packet);
        consumer.accept(new ClientboundSetEntityMotionPacket(getId(), getDeltaMovement()));
    }

    public ElementHolder holder() {
        return this.holder;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isInGround())
            updateRotation();
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        super.setXRot(input.getFloatOr("TridentXRot", 0));
        super.setYRot(input.getFloatOr("TridentYRot", 0));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("TridentXRot", getXRot());
        output.putFloat("TridentYRot", getYRot());
    }
}