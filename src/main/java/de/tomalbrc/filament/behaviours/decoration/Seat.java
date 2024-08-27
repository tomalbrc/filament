package de.tomalbrc.filament.behaviours.decoration;

import de.tomalbrc.filament.api.behaviour.decoration.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.registry.EntityRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Seat behaviours for decoration
 */
public class Seat implements DecorationBehaviour<Seat.SeatConfig> {
    private final SeatConfig seatConfig;

    public Seat(SeatConfig seatConfig) {
        this.seatConfig = seatConfig;
    }

    @Override
    public SeatConfig getConfig() {
        return this.seatConfig;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (decorationBlockEntity.getDecorationHolder() instanceof DecorationHolder decorationHolder) {
            Seat.SeatMeta seat = this.getClosestSeat(decorationBlockEntity, location);

            if (seat != null && !this.hasSeatedPlayer(decorationBlockEntity, seat)) {
                this.seatPlayer(decorationBlockEntity, seat, player);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public void seatPlayer(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat, ServerPlayer player) {
        SeatEntity seatEntity = EntityRegistry.SEAT_ENTITY.create(player.level());
        seatEntity.setPos(this.seatTranslation(decorationBlockEntity, seat).add(decorationBlockEntity.getDecorationHolder().getPos()));
        player.level().addFreshEntity(seatEntity);
        player.startRiding(seatEntity);
        seatEntity.setYRot((decorationBlockEntity.getVisualRotationYInDegrees()-180));
    }

    public boolean hasSeatedPlayer(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat) {
        return !Objects.requireNonNull(decorationBlockEntity.getLevel()).getEntitiesOfClass(SeatEntity.class, AABB.ofSize(seatTranslation(decorationBlockEntity, seat).add(decorationBlockEntity.getDecorationHolder().getPos()), 0.2, 0.2, 0.2), x -> true).isEmpty();
    }

    public Seat.SeatMeta getClosestSeat(DecorationBlockEntity decorationBlockEntity, Vec3 location) {
        if (seatConfig.size() == 1) {
            return seatConfig.get(0);
        }
        else {
            double dist = Double.MAX_VALUE;
            Seat.SeatMeta nearest = null;

            for (Seat.SeatMeta seat : seatConfig) {
                Vec3 q = decorationBlockEntity.getBlockPos().getCenter().add(seatTranslation(decorationBlockEntity, seat));
                double distance = q.distanceTo(location);

                if (!this.hasSeatedPlayer(decorationBlockEntity, seat) && distance < dist) {
                    dist = distance;
                    nearest = seat;
                }
            }

            return nearest;
        }
    }

    public Vec3 seatTranslation(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat) {
        Vec3 v3 = new Vec3(seat.offset).subtract(0, 0.3, 0).yRot((float) Math.toRadians(decorationBlockEntity.getVisualRotationYInDegrees()+180));
        return new Vec3(-v3.x, v3.y, v3.z);
    }

    public static class SeatMeta {
        /**
         * The player seating offset
         */
        public Vector3f offset = new Vector3f();

        /**
         * The rotation direction of the seat
         */
        public float direction = 0;
    }

    public static class SeatConfig extends ObjectArrayList<SeatMeta> { }
}
