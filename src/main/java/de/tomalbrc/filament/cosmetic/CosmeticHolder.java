package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.util.FilamentConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;

public class CosmeticHolder extends ElementHolder {
    private final LivingEntity entity;
    private final ItemDisplayElement displayElement;

    boolean hidden = false;

    private CosmeticInterface access() {
        return (CosmeticInterface) this.entity;
    }

    public CosmeticHolder(LivingEntity entity, ItemStack itemStack) {
        super();

        this.entity = entity;

        this.displayElement = new ItemDisplayElement(itemStack.copy());
        this.displayElement.setInvisible(true);

        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmeticData != null) {
            this.displayElement.setTranslation(cosmeticData.translation);
            this.displayElement.setScale(cosmeticData.scale);
            if (FilamentConfig.getInstance().alternativeCosmeticPlacement) {
                this.displayElement.setItemDisplayContext(ItemDisplayContext.HEAD);
                this.displayElement.setTranslation(new Vector3f(0, 1.25f,0));
                this.displayElement.setScale(new Vector3f(0.625f));
            }
        }

        this.displayElement.setTeleportDuration(1);
        this.displayElement.ignorePositionUpdates();

        this.addElement(this.displayElement);
    }

    @Override
    public void onTick() {
        if (this.entity.isDeadOrDying() || entity.isRemoved()) {
            destroy();
        }

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

            this.displayElement.setYaw(access().filament$bodyYaw());
            this.displayElement.setPitch(this.entity.isShiftKeyDown() ? 25 : 0);
            if (FilamentConfig.getInstance().alternativeCosmeticPlacement) {
                this.displayElement.setTranslation(new Vector3f(0, this.entity.isShiftKeyDown() ? 1.325f : 1.25f, this.entity.isShiftKeyDown() ? 0.15f : 0));
            }
            else {
                Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(this.displayElement.getItem());
                this.displayElement.setTranslation(cosmeticData.translation.add(new Vector3f(0, 0, this.entity.isShiftKeyDown() ? 0.15f : 0), new Vector3f()));
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

    public Vec3 getPos() {
        return this.entity instanceof ServerPlayer ? this.entity.position() : this.entity.getEyePosition();
    }
}
