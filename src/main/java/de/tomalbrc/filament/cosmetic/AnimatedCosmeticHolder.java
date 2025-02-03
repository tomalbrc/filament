package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
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
import java.util.function.Consumer;

public class AnimatedCosmeticHolder extends EntityHolder {
    private final LivingEntity entity;

    private final Consumer<ServerGamePacketListenerImpl> startWatchingCallback;

    boolean hidden = false;

    public AnimatedCosmeticHolder(LivingEntity entity, Model model, Consumer<ServerGamePacketListenerImpl> startWatchingCallback) {
        super(entity, model);
        this.startWatchingCallback = startWatchingCallback;
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
        return this.entity.createCommandSourceStackForNameResolution(this.getLevel()).withMaximumPermission(4);
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        var ret = super.startWatching(player);

        this.startWatchingCallback.accept(player);

        return ret;
    }

    public Vec3 getPos() {
        return this.entity instanceof ServerPlayer ? this.entity.position() : this.entity.getEyePosition();
    }

    @Override
    public void onTick() {
        if (this.entity.getPose() == Pose.SWIMMING || this.entity.getPose() == Pose.SLEEPING) {
            if (!hidden) {
                hideForAll(this);
                hidden = true;
            }
        } else {
            if (hidden) {
                showForAll(this);
                for (ServerGamePacketListenerImpl player : this.getWatchingPlayers()) {
                    startWatchingCallback.accept(player);
                }
                hidden = false;
            }
        }

        super.onTick();
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
