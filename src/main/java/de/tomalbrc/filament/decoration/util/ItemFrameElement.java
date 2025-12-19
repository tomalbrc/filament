package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.mixin.accessor.ItemFrameAccessor;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.DataTrackerLike;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.SimpleDataTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ItemFrameElement extends GenericEntityElement {
    int rotation;
    boolean glow;
    Direction direction;

    public ItemFrameElement(DecorationData decorationData, Direction direction, int rotation, ItemStack itemStack, DecorationUtil.OnInteract onInteract) {
        super();

        this.direction = direction;
        this.rotation = rotation;
        this.glow = decorationData.properties().glow;

        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) 0);
        this.dataTracker.set(ItemFrameAccessor.getDATA_ITEM(), itemStack);
        this.dataTracker.set(ItemFrameAccessor.getDATA_ROTATION(), rotation);
        this.setInvisible(true);

        this.setInteractionHandler(new InteractionHandler() {
            @Override
            public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos) {
                ServerLevel serverLevel = player.level();
                BlockPos blockPos = BlockPos.containing(getHolder().getAttachment().getPos());
                InteractionResult result = InteractionResult.PASS;
                if (onInteract != null && serverLevel.mayInteract(player, blockPos)) {
                    result = onInteract.interact(player, hand, blockPos.getBottomCenter().add(pos));
                }

                if (!result.consumesAction()) DecorationUtil.defaultVirtualInteraction(player, hand, blockPos, pos, 1/16f);
            }

            @Override
            public void attack(ServerPlayer player) {
                ServerLevel serverLevel = player.level();
                BlockPos blockPos = BlockPos.containing(getHolder().getAttachment().getPos());
                player.gameMode.handleBlockBreakAction(blockPos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP, serverLevel.getMaxY(), 0);
            }
        });
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return this.glow ? EntityType.GLOW_ITEM_FRAME : EntityType.ITEM_FRAME;
    }

    @Override
    protected Packet<ClientGamePacketListener> createSpawnPacket(ServerPlayer player) {
        var pos = Objects.requireNonNull(this.getHolder()).getPos().add(this.getOffset());
        return new ClientboundAddEntityPacket(this.getEntityId(), this.getUuid(), pos.x, pos.y, pos.z, this.getPitch(), this.getYaw(), this.getEntityType(), this.direction.get3DDataValue(), Vec3.ZERO, this.getYaw());
    }

    @Override
    protected DataTrackerLike createDataTracker() {
        return new SimpleDataTracker(EntityType.ITEM_FRAME);
    }

    @Override
    public void setYaw(float yaw) {
        this.dataTracker.set(ItemFrameAccessor.getDATA_ROTATION(), Util.SEGMENTED_ANGLE8.fromDegrees(yaw-180));
    }

    public void setItem(ItemStack item) {
        this.dataTracker.set(ItemFrameAccessor.getDATA_ITEM(), item.copy());
    }
}