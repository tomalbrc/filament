package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.util.SeatEntity;
import de.tomalbrc.filament.registry.EntityRegistry;
import de.tomalbrc.filament.util.FilamentConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

/**
 * Seat behaviour for decoration
 */
public class Seat implements DecorationBehaviour<Seat.SeatConfig> {
    private final SeatConfig seatConfig;

    public Seat(SeatConfig seatConfig) {
        this.seatConfig = seatConfig;
    }

    @Override
    @NotNull
    public SeatConfig getConfig() {
        return this.seatConfig;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (player.getVehicle() == null && !player.isSecondaryUseActive() && decorationBlockEntity.getDecorationHolder() instanceof DecorationHolder) {
            Seat.SeatMeta seat = this.getClosestSeat(decorationBlockEntity, location);

            if (seat != null && !this.hasSeatedPlayer(decorationBlockEntity, seat)) {
                this.seatPlayer(decorationBlockEntity, seat, player);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public void seatPlayer(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat, ServerPlayer player) {
        SeatEntity seatEntity = EntityRegistry.SEAT_ENTITY.create(player.serverLevel());
        assert seatEntity != null;
        seatEntity.setPos(this.seatTranslation(decorationBlockEntity, seat).add(decorationBlockEntity.getDecorationHolder().getPos()));
        player.level().addFreshEntity(seatEntity);
        player.startRiding(seatEntity);
        seatEntity.setYRot((decorationBlockEntity.getVisualRotationYInDegrees() - seat.direction + (FilamentConfig.getInstance().alternativeBlockPlacement ? 180 : 0)));
    }

    public boolean hasSeatedPlayer(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat) {
        return !Objects.requireNonNull(decorationBlockEntity.getLevel()).getEntitiesOfClass(SeatEntity.class, AABB.ofSize(seatTranslation(decorationBlockEntity, seat).add(decorationBlockEntity.getDecorationHolder().getPos()), 0.2, 0.2, 0.2), x -> true).isEmpty();
    }

    public Seat.SeatMeta getClosestSeat(DecorationBlockEntity decorationBlockEntity, Vec3 location) {
        if (seatConfig.size() == 1) {
            return seatConfig.getFirst();
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
        Vec3 v3 = new Vec3(seat.offset).subtract(0, 0.3, 0).yRot((float) Math.toRadians(decorationBlockEntity.getVisualRotationYInDegrees()+(FilamentConfig.getInstance().alternativeBlockPlacement ? 0 : 180)));
        return new Vec3(-v3.x, v3.y, v3.z);
    }

    public SeatEntity getSeatEntity(DecorationBlockEntity decorationBlockEntity, Seat.SeatMeta seat) {
        List<SeatEntity> entities = Objects.requireNonNull(decorationBlockEntity.getLevel()).getEntitiesOfClass(SeatEntity.class, AABB.ofSize(seatTranslation(decorationBlockEntity, seat).add(decorationBlockEntity.getDecorationHolder().getPos()), 0.2, 0.2, 0.2), x -> true);
        if (!entities.isEmpty())
            return entities.getFirst();

        return null;
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        for (SeatMeta seatMeta : this.seatConfig) {
            var seat = getSeatEntity(decorationBlockEntity, seatMeta);
            if (seat != null && seat.getFirstPassenger() != null) {
                seat.getFirstPassenger().stopRiding();
            }
        }
    }

    public static class SeatMeta {
        /**
         * The player seating offset
         */
        public Vector3f offset = new Vector3f();

        /**
         * The rotation direction of the seat
         */
        public float direction = 180;
    }

    public static class SeatConfig extends ObjectArrayList<SeatMeta> { }
}
