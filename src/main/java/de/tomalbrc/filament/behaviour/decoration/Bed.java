package de.tomalbrc.filament.behaviour.decoration;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.mixin.accessor.PlayerAccessor;
import de.tomalbrc.filament.util.DecorationUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * For beds
 */
public class Bed implements DecorationBehaviour<Bed.Config> {

    private final Config config;

    public Bed(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Bed.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        return this.startSleeping(player, decorationBlockEntity, decorationBlockEntity.getBlockPos());
    }

    private InteractionResult startSleeping(ServerPlayer player, DecorationBlockEntity decorationBlockEntity, BlockPos blockPos) {
        var res = this.startSleepInBed(player, decorationBlockEntity, blockPos);
        res.ifLeft((bedSleepingProblem) -> {
            if (bedSleepingProblem.getMessage() != null) {
                player.displayClientMessage(bedSleepingProblem.getMessage(), true);
            }
        });

        return res.right().isPresent() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    Pair<Boolean, Boolean> inRangeOrBlocked(ServerPlayer serverPlayer, DecorationBlockEntity decorationBlockEntity) {
        AtomicBoolean range = new AtomicBoolean(false);
        AtomicBoolean blocked = new AtomicBoolean(false);
        DecorationUtil.forEachRotated(decorationBlockEntity.getDecorationData().blocks(), decorationBlockEntity.mainPosition(), decorationBlockEntity.getBlock().getVisualRotationYInDegrees(decorationBlockEntity.getBlockState()), blockPos -> {
            if (serverPlayer.level().getBlockState(blockPos.above()).isSuffocating(serverPlayer.level(), blockPos)) {
                blocked.set(true);
            }

            if (isReachableBedBlock(serverPlayer, blockPos)) {
                range.set(true);
            }
        });
        return Pair.of(range.get(), blocked.get());
    }

    private boolean isReachableBedBlock(ServerPlayer serverPlayer, BlockPos blockPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
        return Math.abs(serverPlayer.getX() - vec3.x()) <= 3.0F && Math.abs(serverPlayer.getY() - vec3.y()) <= 2.0F && Math.abs(serverPlayer.getZ() - vec3.z()) <= 3.0F;
    }

    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(ServerPlayer player, DecorationBlockEntity decorationBlockEntity, BlockPos blockPos) {
        if (!player.isSleeping() && player.isAlive()) {
            Pair<Boolean, Boolean> testRes = inRangeOrBlocked(player, decorationBlockEntity);
            if (!player.level().dimensionType().natural()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
            } else if (!testRes.getFirst()) {
                return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
            } else if (testRes.getSecond()) {
                return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
            } else {
                player.setRespawnPosition(new ServerPlayer.RespawnConfig(player.level().dimension(), blockPos, player.getYRot(), false), true);
                if (player.level().isBrightOutside()) {
                    return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
                } else {
                    if (!player.isCreative()) {
                        double hRange = 8.0F;
                        double vRange = 5.0F;
                        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                        List<Monster> list = player.level().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - hRange, vec3.y() - vRange, vec3.z() - hRange, vec3.x() + hRange, vec3.y() + vRange, vec3.z() + hRange), (monster) -> monster.isPreventingPlayerRest(player.level(), player));
                        if (!list.isEmpty()) {
                            return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                        }
                    }

                    Either<Player.BedSleepingProblem, Unit> either = this.startSleepInBedDirect(player, blockPos).ifRight((unit) -> {
                        player.awardStat(Stats.SLEEP_IN_BED);
                        CriteriaTriggers.SLEPT_IN_BED.trigger(player);
                    });
                    if (!player.level().canSleepThroughNights()) {
                        player.displayClientMessage(Component.translatable("sleep.not_possible"), true);
                    }

                    player.level().updateSleepingPlayerList();
                    return either;
                }
            }
        } else {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    public Either<Player.BedSleepingProblem, Unit> startSleepInBedDirect(ServerPlayer entity, BlockPos blockPos) {
        entity.startSleeping(blockPos);
        ((PlayerAccessor)entity).setSleepCounter(0);
        return Either.right(Unit.INSTANCE);
    }

    public static class Config {
        boolean skipNight = false;
    }
}