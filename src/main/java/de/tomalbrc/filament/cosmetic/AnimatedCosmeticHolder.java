package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public class AnimatedCosmeticHolder extends EntityHolder {
    private final LivingEntity entity;
    private double prevX = 0;
    private double prevZ = 0;

    private float bodyYaw;

    private final Consumer<ServerGamePacketListenerImpl> startWatchingCallback;

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

    @Override
    protected void updateElement(DisplayWrapper<?> display) {
        display.element().ignorePositionUpdates();
        super.updateElement(display);

        display.element().setYaw(this.bodyYaw);

        this.prevX = this.entity.getX();
        this.prevZ = this.entity.getZ();
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        return this.entity.createCommandSourceStack().withMaximumPermission(4);
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
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        var ret = super.startWatching(player);

        this.startWatchingCallback.accept(player);

        return ret;
    }

    @Override
    public void onTick() {
        super.onTick();

        this.tickMovement(this.entity);

        // manual ticking
        //this.onAsyncTick();
    }
}
