package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.bil.core.holder.base.AbstractAnimationHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.holder.positioned.PositionedHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class AnimatedCosmeticHolder extends EntityHolder {
    private final ServerPlayer player;
    private double prevX = 0;
    private double prevZ = 0;

    private float bodyYaw;

    public AnimatedCosmeticHolder(ServerPlayer player, Model model) {
        super(player, model);

        this.player = player;
    }

    @Override
    protected void updateElement(DisplayWrapper<?> display) {
        display.element().ignorePositionUpdates();
        super.updateElement(display);

        display.element().setYaw(this.bodyYaw);

        this.prevX = this.player.getX();
        this.prevZ = this.player.getZ();
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        return this.player.createCommandSourceStack();
    }

    private void tickMovement(final ServerPlayer player) {
        float yaw = this.player.getYRot();
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

        player.send(VirtualEntityUtils.createRidePacket(this.player.getId(), this.getDisplayIds()));

        return ret;
    }

    @Override
    public void onTick() {
        super.onTick();

        this.tickMovement(this.player);

        // manual ticking
        //this.onAsyncTick();
    }
}
