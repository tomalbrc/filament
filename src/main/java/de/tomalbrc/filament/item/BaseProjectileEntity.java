package de.tomalbrc.filament.item;

import com.mojang.math.Axis;
import de.tomalbrc.filament.behaviour.item.Shoot;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

public class BaseProjectileEntity extends AbstractArrow implements PolymerEntity {
    protected ItemStack projectileStack = Items.DIRT.getDefaultInstance();
    protected ItemStack pickupStack;
    private boolean dealtDamage;
    protected final ElementHolder holder = new ElementHolder() {
        @Override
        protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
        }

        @Override
        protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
            packetConsumer.accept(new ClientboundSetPassengersPacket(BaseProjectileEntity.this));
        }
    };

    protected ItemDisplayElement mainDisplayElement = new ItemDisplayElement();
    protected final InteractionElement interactionElement = new InteractionElement(); // InteractionElement.redirect(this);
    protected final InteractionElement interactionElement2 = new InteractionElement(); // InteractionElement.redirect(this);

    public Shoot.ShootConfig config;

    protected void createMainDisplayElement() {
        this.mainDisplayElement.setItem(this.projectileStack);

        if (!this.holder.getElements().contains(this.mainDisplayElement)) {
            this.holder.addElement(this.mainDisplayElement);
            VirtualEntityUtils.addVirtualPassenger(this, this.mainDisplayElement.getEntityId());
        }

        this.mainDisplayElement.setLeftRotation(Axis.ZP.rotationDegrees(180));
        this.mainDisplayElement.setRightRotation(new Quaternionf().rotateY((float) Math.toRadians(getYRot())).mul(config.rotation).normalize());
        this.mainDisplayElement.setTranslation(new Vector3f(0.f, 0.15f, 0.f).add(config.translation));
        this.mainDisplayElement.setScale(config.scale);
    }

    public BaseProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world); // use empty for now, if the future should use the pickupItem of AbstractArrow added in 1.20.3

        EntityAttachment.of(this.holder, this);

        this.interactionElement.setSize(0.5f, 0.25f);
        this.interactionElement2.setSize(0.5f, -0.25f);

        this.holder.addElement(this.interactionElement);
        this.holder.addElement(this.interactionElement2);

        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement.getEntityId());
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement2.getEntityId());
    }

    public ItemDisplayElement getDisplayElement() {
        return this.mainDisplayElement;
    }

    public void setProjectileStack(ItemStack projectileStack) {
        this.projectileStack = projectileStack;
        this.createMainDisplayElement();
    }

    public void setPickupStack(ItemStack itemStack) {
        this.pickupStack = itemStack;
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        super.tick();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 currentPosition, Vec3 nextPosition) {
        return this.dealtDamage ? null : super.findHitEntity(currentPosition, nextPosition);
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_SMALL | ArmorStand.CLIENT_FLAG_MARKER)));
    }

    @Override
    @NotNull
    protected ItemStack getPickupItem() {
        return this.pickupStack == null ? this.projectileStack : this.pickupStack.copy();
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (entityHitResult.getEntity() instanceof LivingEntity target) {
            this.dealtDamage = true;
            Entity owner = this.getOwner();
            var damageSource = this.damageSources().trident(this, owner);

            float damage = (float) this.getBaseDamage();
            if (target.hurt(damageSource, damage)) {
                if (target.getType() != EntityType.ENDERMAN && this.getOwner() instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel) target.level(), livingOwner, damageSource, this.getWeaponItem());
                    this.doPostHurtEffects(target);
                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected boolean tryPickup(Player player) {
        // todo: animate projectile towards player and scale down a bit
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("Item", 10)) this.projectileStack = ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("Item"));
        if (nbt.contains("PickupItem", 10)) this.pickupStack = ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("PickupItem"));

        this.createMainDisplayElement();

        this.dealtDamage = nbt.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        if (projectileStack != null && !projectileStack.isEmpty()) nbt.put("Item", this.projectileStack.save(this.registryAccess()));
        if (pickupStack != null && !pickupStack.isEmpty())nbt.put("PickupItem", this.pickupStack.save(this.registryAccess()));

        nbt.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }
}