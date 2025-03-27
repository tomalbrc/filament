package de.tomalbrc.filament.mixin.behaviour.cosmetic;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentCosmeticEvents;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.cosmetic.AnimatedCosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.cosmetic.CosmeticUtil;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.registry.FilamentComponents;
import de.tomalbrc.filament.registry.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin implements CosmeticInterface {
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

    @Shadow public abstract EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack);

    @Unique
    private final Map<String, ElementHolder> filamentCosmeticHolder = new Object2ObjectOpenHashMap<>();

    @Unique
    private double filamentPrevX = 0;
    @Unique
    private double filamentPrevZ = 0;
    @Unique
    private double filamentBodyYaw;

    @Unique
    boolean filamentEquipAfterLoad = true;

    // COSMETIC, ARMOR, ELYTRA
    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (itemStack.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var wrapped = itemStack.get(FilamentComponents.SKIN_DATA_COMPONENT);
            var es = getEquipmentSlotForItem(wrapped);
            if (es != EquipmentSlot.MAINHAND) {
                cir.setReturnValue(es);
                return;
            }
        }

        Cosmetic.Config cosmetic = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmetic != null) {
            cir.setReturnValue(cosmetic.slot);
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD"))
    private void filament$customOnEquipItem(EquipmentSlot equipmentSlot, ItemStack oldItemStack, ItemStack newItemStack, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.HEAD || equipmentSlot == EquipmentSlot.OFFHAND || equipmentSlot == EquipmentSlot.MAINHAND) {
            return;
        }

        if (CosmeticUtil.isCosmetic(oldItemStack)) {
            var component = oldItemStack.get(DataComponents.EQUIPPABLE);
            var slot = component == null ? Objects.requireNonNull(((SimpleItem)oldItemStack.getItem()).get(Behaviours.COSMETIC)).getConfig().slot : component.slot();
            if (slot == equipmentSlot) {
                filament$destroyHolder(slot.getName());
                FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), oldItemStack, newItemStack);
            }
        }

        // hotswap case
        {
            if (oldItemStack.isEmpty() && !CosmeticUtil.isCosmetic(this.getItemBySlot(this.getEquipmentSlotForItem(newItemStack)))) {
                var component = newItemStack.get(DataComponents.EQUIPPABLE);
                if (component != null) {
                    filament$destroyHolder(component.slot().getName());
                    FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), oldItemStack, newItemStack);
                }
            }
        }

        if (CosmeticUtil.isCosmetic(newItemStack)) {
            var component = newItemStack.get(DataComponents.EQUIPPABLE);
            var slot = component == null ? Objects.requireNonNull(((SimpleItem)newItemStack.getItem()).get(Behaviours.COSMETIC)).getConfig().slot : component.slot();
            if (slot == equipmentSlot || oldItemStack.isEmpty()) {
                filament$destroyHolder(slot.getName());
                FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), oldItemStack, newItemStack);
                filament$addHolder(LivingEntity.class.cast(this), newItemStack.getItem(), newItemStack, slot.getName());
                FilamentCosmeticEvents.EQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), oldItemStack, newItemStack);
            }
        }
    }

    @Inject(method = "remove", at = @At(value = "HEAD"))
    private void filament$onRemove(Entity.RemovalReason removalReason, CallbackInfo ci) {
        var self = LivingEntity.class.cast(this);
        for(EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (CosmeticUtil.isCosmetic(itemStack)) {
                filament$destroyHolder(self.getEquipmentSlotForItem(itemStack).getName());
                FilamentCosmeticEvents.UNEQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), itemStack, ItemStack.EMPTY);
            }
        }
    }

    @Unique
    @Override
    public void filament$addHolder(LivingEntity livingEntity, Item simpleItem, ItemStack itemStack, String slot) {
        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(itemStack);

        ElementHolder holder = null;

        if (cosmeticData.model != null && !filamentCosmeticHolder.containsKey(slot)) {
            holder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model));
        }
        else if (!filamentCosmeticHolder.containsKey(slot)) {
            holder = new CosmeticHolder(livingEntity, itemStack);
        }

        if (holder == null) {
            Filament.LOGGER.error("Could not create cosmetic holder");
            return;
        }

        EntityAttachment.ofTicking(holder, livingEntity);

        if (livingEntity instanceof ServerPlayer serverPlayer)
            holder.startWatching(serverPlayer);

        VirtualEntityUtils.addVirtualPassenger(livingEntity, holder.getEntityIds().toIntArray());

        var packet = VirtualEntityUtils.createRidePacket(livingEntity.getId(), ((EntityExt)livingEntity).polymerVE$getVirtualRidden());
        if (livingEntity instanceof ServerPlayer serverPlayer)
            serverPlayer.connection.send(packet);

        if (cosmeticData.autoplay != null && holder instanceof AnimatedCosmeticHolder animatedHolder) {
            animatedHolder.getAnimator().playAnimation(cosmeticData.autoplay);
        }

        filamentCosmeticHolder.put(slot, holder);
    }

    @Unique
    @Override
    public void filament$destroyHolder(String slot) {
        if (filamentCosmeticHolder.containsKey(slot)) {
            var holder = filamentCosmeticHolder.get(slot);

            VirtualEntityUtils.removeVirtualPassenger(LivingEntity.class.cast(this), holder.getEntityIds().toIntArray());

            var attachment = filamentCosmeticHolder.get(slot).getAttachment();
            if (attachment != null) {
                attachment.destroy();
            }
            filamentCosmeticHolder.get(slot).destroy();
            filamentCosmeticHolder.remove(slot);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void rotationTick(CallbackInfo ci) {
        var self = LivingEntity.class.cast(this);
        var isPlayer = (self instanceof Player);

        if (filamentEquipAfterLoad && !isPlayer) {
            for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR.slots()) {
                var stack = this.getItemBySlot(slot);
                if (CosmeticUtil.isCosmetic(stack) && slot != EquipmentSlot.HEAD) {
                    filament$addHolder(self, stack.getItem(), stack, slot.getName());
                    FilamentCosmeticEvents.EQUIPPED.invoker().unequipped(LivingEntity.class.cast(this), ItemStack.EMPTY, stack);
                }
            }
            filamentEquipAfterLoad = false;
        }

        if (!filamentCosmeticHolder.isEmpty() && (filamentPrevX != 0 && filamentPrevZ != 0) && isPlayer)
            filament$tickMovement(self);
        else
            this.filamentBodyYaw = self.yBodyRot;

        filamentPrevX = self.getX();
        filamentPrevZ = self.getZ();
    }

    @Unique
    private void filament$tickMovement(final LivingEntity entity) {
        double yaw = entity.getYRot();
        double i = entity.getX() - this.filamentPrevX;
        double d = entity.getZ() - this.filamentPrevZ;
        double f = (float)(i * i + d * d);
        double g = this.filamentBodyYaw;
        if (f > 0.0025) {
            double l = Math.atan2(d, i) * (double)Mth.RAD_TO_DEG - 90.;
            double m = Math.abs(Mth.wrapDegrees(yaw) - l);
            if (95. < m && m < 265.) {
                g = l - 180.;
            } else {
                g = l;
            }
        }

        this.filament$turnBody(g, yaw);
    }

    @Unique
    public void filament$turnBody(double bodyRotation, double yaw) {
        double f = Mth.wrapDegrees(bodyRotation - this.filamentBodyYaw);
        this.filamentBodyYaw += f * 0.3F;
        double g = Mth.wrapDegrees(yaw - this.filamentBodyYaw);
        if (g < -75.) {
            g = -75.;
        }

        if (g >= 75.) {
            g = 75.;
        }

        this.filamentBodyYaw = yaw - g;
        if (g * g > 2500.) { // > 50°
            this.filamentBodyYaw += g * 0.25;
        }
    }

    @Override
    @Unique
    public float filament$bodyYaw() {
        return (float) filamentBodyYaw;
    }
}
