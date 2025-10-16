package de.tomalbrc.filament.item;

import com.mojang.math.Axis;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Shoot;
import de.tomalbrc.filament.mixin.accessor.AbstractArrowAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
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
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;

public class BaseProjectileEntity extends AbstractArrow implements PolymerEntity {
    protected ItemStack projectileStack = Items.DIRT.getDefaultInstance();
    protected ItemStack pickupStack;
    protected ItemStack base;
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

    public Shoot.Config config;

    protected void createMainDisplayElement() {
        this.mainDisplayElement.setItem(this.projectileStack);

        if (!this.holder.getElements().contains(this.mainDisplayElement)) {
            this.holder.addElement(this.mainDisplayElement);
            VirtualEntityUtils.addVirtualPassenger(this, this.mainDisplayElement.getEntityId());
        }

        this.mainDisplayElement.setItemDisplayContext(config.display);
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

    public void setBase(ItemStack itemStack) {
        ((AbstractArrowAccessor)this).setFiredFromWeapon(itemStack);
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
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
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
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (config.dropAsItem && !level().isClientSide()) {
            spawnAtLocation((ServerLevel) level(), getPickupItem());
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (entityHitResult.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity livingEntity) {
            this.dealtDamage = true;
            var damageSource = this.damageSources().mobProjectile(this, livingEntity);

            double damage = ((AbstractArrowAccessor)this).getBaseDamage();
            if (target.hurtServer((ServerLevel) level(), damageSource, (float) damage)) {
                if (target.getType() != EntityType.ENDERMAN && this.getOwner() instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel) target.level(), livingOwner, damageSource, this.getWeaponItem());
                    this.doPostHurtEffects(target);
                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvent.createVariableRangeEvent(config.hitSound), config.hitVolume, config.hitPitch);
    }

    @Override
    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvent.createVariableRangeEvent(config.hitGroundSound);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.noPhysics || this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.base = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.projectileStack = input.read("ProjectileItem", ItemStack.CODEC).orElse(base);
        this.pickupStack = input.read("PickupItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        if (this.base.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.SHOOT))
            this.config = simpleItem.getOrThrow(Behaviours.SHOOT).getConfig();

        this.createMainDisplayElement();

        this.dealtDamage = input.getBooleanOr("DealtDamage", true);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        if (this.projectileStack != null && !this.projectileStack.isEmpty()) output.store("Item", ItemStack.CODEC, this.projectileStack);
        if (this.pickupStack != null && !this.pickupStack.isEmpty()) output.store("PickupItem", ItemStack.CODEC, this.pickupStack);

        output.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return config.canPickUp && super.tryPickup(player);
    }
}