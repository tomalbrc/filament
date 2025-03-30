package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.mixin.ItemFrameAccessor;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.mixin.entity.ItemFrameEntityAccessor;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.DataTrackerLike;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.SimpleDataTracker;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

public class ItemFrameElement extends GenericEntityElement {
    final private DecorationBlockEntity parent;

    public ItemFrameElement(DecorationBlockEntity source) {
        super();

        this.parent = source;

        this.sendTrackerUpdates();

        var stack = source.getItem();
        if (stack.getItem() instanceof PolymerItem polymerItem) {
            stack = polymerItem.getPolymerItemStack(stack, TooltipFlag.NORMAL, PacketContext.create());
        }

        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) 0);
        this.dataTracker.set(ItemFrameEntityAccessor.getITEM_STACK(), stack.copy());
        this.dataTracker.set(ItemFrameAccessor.getDATA_ROTATION(), Math.max(source.getRotation(), 1));
        this.setInvisible(true);

        this.setInteractionHandler(new InteractionHandler() {
            @Override
            public void interact(ServerPlayer player, InteractionHand hand) {
                InteractionHandler.super.interact(player, hand);
                source.interact(player, hand, player.position());
            }

            @Override
            public void attack(ServerPlayer player) {
                InteractionHandler.super.attack(player);
                source.destroyStructure(true);
            }
        });
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return parent.getDecorationData().properties().glow ? EntityType.GLOW_ITEM_FRAME : EntityType.ITEM_FRAME;
    }

    @Override
    protected Packet<ClientGamePacketListener> createSpawnPacket(ServerPlayer player) {
        var pos = Objects.requireNonNull(this.getHolder()).getPos().add(this.getOffset());
        return new ClientboundAddEntityPacket(this.getEntityId(), this.getUuid(), pos.x, pos.y, pos.z, this.getPitch(), this.getYaw(), this.getEntityType(), this.parent.getDirection().get3DDataValue(), Vec3.ZERO, this.getYaw());
    }

    @Override
    protected DataTrackerLike createDataTracker() {
        return new SimpleDataTracker(EntityType.ITEM_FRAME);
    }
}