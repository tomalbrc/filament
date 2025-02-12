package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class AnimatedCosmeticHolder extends EntityHolder {
    private final LivingEntity entity;
    private boolean hidden = false;

    public AnimatedCosmeticHolder(LivingEntity entity, Model model) {
        super(entity, model);
        this.entity = entity;
    }

    @Override
    public int getVehicleId() {
        return -1;
    }

    @Override
    public boolean addAdditionalDisplay(DisplayElement element) {
        if (this.additionalDisplays.add(element)) {
            this.addElement(element);
            return true;
        } else {
            return false;
        }
    }

    private CosmeticInterface access() {
        return (CosmeticInterface) this.entity;
    }

    @Override
    protected void updateElement(DisplayWrapper<?> display) {
        display.element().ignorePositionUpdates();
        display.element().setYaw(access().filament$bodyYaw());
        super.updateElement(display);
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        return this.entity.createCommandSourceStack().withMaximumPermission(4);
    }

    public Vec3 getPos() {
        return this.entity instanceof ServerPlayer ? this.entity.position() : this.entity.getEyePosition();
    }

    @Override
    public void onTick() {
        if (this.entity.getPose() == Pose.SWIMMING || this.entity.getPose() == Pose.SLEEPING || (this.entity instanceof  ServerPlayer serverPlayer && serverPlayer.isSpectator())) {
            if (!hidden) {
                hideForAll(this);

                hidden = true;
            }
        } else {
            if (hidden) {
                showForAll(this);

                this.updatePosition();
                var packet = VirtualEntityUtils.createRidePacket(entity.getId(), ((EntityExt)entity).polymerVE$getVirtualRidden());
                this.sendPacket(packet);

                hidden = false;
            }
        }

        super.onTick();
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    public static void hideForAll(ElementHolder elementHolder) {
        for (ServerGamePacketListenerImpl player : elementHolder.getWatchingPlayers()) {
            player.send(new ClientboundRemoveEntitiesPacket(elementHolder.getEntityIds()));
        }
    }

    public static void showForAll(ElementHolder elementHolder) {
        for (ServerGamePacketListenerImpl player : elementHolder.getWatchingPlayers()) {
            var packets = new ObjectArrayList<Packet<? super ClientGamePacketListener>>();
            for (VirtualElement e : elementHolder.getElements()) {
                Objects.requireNonNull(packets);
                e.startWatching(player.player, packets::add);
            }
            player.send(new ClientboundBundlePacket(packets));
        }
    }
}
