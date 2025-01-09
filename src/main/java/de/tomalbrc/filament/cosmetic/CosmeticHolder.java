package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.util.FilamentConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.function.Consumer;

public class CosmeticHolder extends ElementHolder {
    private final LivingEntity entity;
    private final ItemDisplayElement displayElement;

    private double prevX = 0;
    private double prevZ = 0;

    private float bodyYaw;

    private final Consumer<ServerGamePacketListenerImpl> startWatchingCallback;

    boolean hidden = false;

    public CosmeticHolder(LivingEntity entity, ItemStack itemStack, Consumer<ServerGamePacketListenerImpl> startWatchingCallback) {
        super();

        this.startWatchingCallback = startWatchingCallback;

        this.entity = entity;

        this.displayElement = new ItemDisplayElement(itemStack);

        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmeticData != null) {
            this.displayElement.setTranslation(cosmeticData.translation);
            this.displayElement.setScale(cosmeticData.scale);
            if (FilamentConfig.getInstance().alternativeCosmeticPlacement) {
                this.displayElement.setModelTransformation(ItemDisplayContext.HEAD);
                this.displayElement.setTranslation(new Vector3f(0, 1.25f,0));
                this.displayElement.setScale(new Vector3f(0.625f));
            }
        }

        this.displayElement.setTeleportDuration(1);

        this.addElement(this.displayElement);
    }

    @Override
    public void onTick() {
        super.onTick();

        this.tickMovement(this.entity);

        if (this.entity.getPose() == Pose.SWIMMING) {
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

            this.displayElement.setYaw(this.bodyYaw);
            this.displayElement.setPitch(this.entity.isShiftKeyDown() ? 25 : 0);

            if (FilamentConfig.getInstance().alternativeCosmeticPlacement) {
                this.displayElement.setTranslation(new Vector3f(0, this.entity.isShiftKeyDown() ? 1.325f : 1.25f, this.entity.isShiftKeyDown() ? 0.15f : 0));
            }
            else {
                Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(this.displayElement.getItem());
                this.displayElement.setTranslation(cosmeticData.translation.add(new Vector3f(0, 0, this.entity.isShiftKeyDown() ? 0.15f : 0)));
            }
        }

        this.prevX = this.entity.getX();
        this.prevZ = this.entity.getZ();
    }

    private void tickMovement(final LivingEntity player) {
        float yaw = this.entity.getYRot();
        double i = player.getX() - this.prevX;
        double d = player.getZ() - this.prevZ;
        float f = (float)(i * i + d * d);
        float g = this.bodyYaw;
        if (f > 0.0025f) {
            // Using internal Mojang math utils here
            float l = (float) Mth.atan2(d, i) * Mth.RAD_TO_DEG - 90.0F;
            float m = Mth.abs(Mth.wrapDegrees(yaw) - l);
            if (95.f < m && m < 265.f) {
                g = l - 180.f;
            } else {
                g = l;
            }
        }

        this.turnBody(g, yaw);
    }

    public void turnBody(float bodyRotation, float yaw) {
        float f = Mth.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += f * 0.3F;
        float g = Mth.wrapDegrees(yaw - this.bodyYaw);
        if (g < -75.0F) {
            g = -75.0F;
        }

        if (g >= 75.0F) {
            g = 75.0F;
        }

        this.bodyYaw = yaw - g;
        if (g * g > 2500.0F) {
            this.bodyYaw += g * 0.2F;
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }
    @Override
    protected void updateInitialPosition() {
        if (this.entity instanceof ServerPlayer serverPlayer) this.startWatching(serverPlayer);
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        var ret = super.startWatching(player);
        this.startWatchingCallback.accept(player);
        return ret;
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
