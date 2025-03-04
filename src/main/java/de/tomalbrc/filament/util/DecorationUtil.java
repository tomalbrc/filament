package de.tomalbrc.filament.util;

import com.mojang.math.Axis;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Math;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DecorationUtil {
    public static void forEachRotated(List<DecorationData.BlockConfig> blockConfigs, BlockPos originBlockPos, float rotation, Consumer<BlockPos> consumer) {
        if (blockConfigs != null) {
            for (DecorationData.BlockConfig blockConfig : blockConfigs) {
                Vector3fc origin = blockConfig.origin();
                Vector3fc size = blockConfig.size();
                for (int x = 0; x < size.x(); x++) {
                    for (int y = 0; y < size.y(); y++) {
                        for (int z = 0; z < size.z(); z++) {
                            Vector3f pos = new Vector3f(x, y, z).add(origin);
                            Vector3f offset = pos.mul(rotation % 90 != 0 ? org.joml.Math.sqrt(2) : 1);
                            offset.rotateY(org.joml.Math.toRadians(rotation + (FilamentConfig.getInstance().alternativeBlockPlacement ? 0 : 180)));

                            BlockPos blockPos = new BlockPos(originBlockPos).offset(-org.joml.Math.round(offset.x), org.joml.Math.round(offset.y), Math.round(offset.z));
                            consumer.accept(blockPos);
                        }
                    }
                }
            }
        }
    }

    public static Vector2f barrierDimensions(List<DecorationData.BlockConfig> blockConfigs, float rotation) {
        if (!blockConfigs.isEmpty()) {
            List<BlockPos> posList = new ObjectArrayList<>();
            DecorationUtil.forEachRotated(blockConfigs, BlockPos.ZERO, rotation, posList::add);
            Optional<BoundingBox> boundingBox = BoundingBox.encapsulatingPositions(posList);
            if (boundingBox.isPresent()) {
                return new Vector2f(java.lang.Math.max(boundingBox.get().getXSpan(), boundingBox.get().getZSpan()), boundingBox.get().getYSpan());
            }
        }
        return new Vector2f();
    }

    public static InteractionElement decorationInteraction(DecorationBlockEntity blockEntity) {
        InteractionElement element = new InteractionElement();
        element.setHandler(new VirtualElement.InteractionHandler() {
            @Override
            public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos) {
                ServerLevel serverLevel = element.getHolder().getAttachment().getWorld();
                BlockPos blockPos = BlockPos.containing(element.getHolder().getAttachment().getPos());
                if (serverLevel.mayInteract(player, blockPos)) {
                    blockEntity.interact(player, hand, blockEntity.getBlockPos().getCenter().subtract(0, 0.5f, 0).add(pos));
                }
            }

            @Override
            public void attack(ServerPlayer player) {
                ServerLevel serverLevel = element.getHolder().getAttachment().getWorld();
                BlockPos blockPos = BlockPos.containing(element.getHolder().getAttachment().getPos());
                player.gameMode.handleBlockBreakAction(blockPos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP, serverLevel.getMaxY(), 0);
            }
        });

        if (blockEntity.getDecorationData() != null && blockEntity.getDecorationData().size() != null) {
            element.setSize(blockEntity.getDecorationData().size().x, blockEntity.getDecorationData().size().y);
        } else {
            element.setSize(1.f, blockEntity.getDirection().equals(Direction.DOWN) ? 1.f : .5f); // default
        }

        var q = blockEntity.getDirection().getUnitVec3i();
        if (blockEntity.getDirection() != Direction.DOWN && blockEntity.getDirection() != Direction.UP) {
            element.setOffset(new Vec3(q.getX(), q.getY() + element.getHeight(), q.getZ()).multiply(1.f-element.getWidth(), 1, 1.f-element.getWidth()).scale(-0.5f));
        } else {
            element.setOffset(new Vec3(q.getX(), q.getY(), q.getZ()).add(0,  blockEntity.getDirection() == Direction.UP ? -1.5f : 0.5f + ((1.f-element.getHeight())), 0));
        }

        return element;
    }

    public static InteractionElement decorationInteraction(DecorationData decorationData) {
        InteractionElement element = new InteractionElement();
        element.setHandler(new VirtualElement.InteractionHandler() {
            @Override
            public void attack(ServerPlayer player) {
                ServerLevel serverLevel = element.getHolder().getAttachment().getWorld();
                BlockPos blockPos = BlockPos.containing(element.getHolder().getAttachment().getPos());
                player.gameMode.handleBlockBreakAction(blockPos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP, serverLevel.getMaxY(), 0);
            }
        });
        element.setSize(1.f, 1.f);

        if (decorationData.size() != null) {
            element.setSize(decorationData.size().x, decorationData.size().y);
        } else {
            element.setSize(1.f, 1.f);
        }

        element.setOffset(new Vec3(0, -0.5f, 0));

        return element;
    }

    public static ItemDisplayElement decorationItemDisplay(DecorationBlockEntity blockEntity) {
        ItemDisplayElement display = decorationItemDisplay(blockEntity.getDecorationData(), blockEntity.getDirection(), blockEntity.getVisualRotationYInDegrees());
        display.setItem(blockEntity.getItem().copyWithCount(1));
        return display;
    }

    public static ItemDisplayElement decorationItemDisplay(DecorationData data, Direction direction, float rotation) {
        ItemDisplayElement itemDisplayElement = new ItemDisplayElement(BuiltInRegistries.ITEM.getValue(data.id()));
        itemDisplayElement.setTeleportDuration(1);

        if (data != null && data.properties().glow) {
            itemDisplayElement.setBrightness(Brightness.FULL_BRIGHT);
        }

        Vector2f size = new Vector2f(1);
        if (data.hasBlocks()) {
            size = DecorationUtil.barrierDimensions(data.blocks(), rotation);
        } else if (data.size() != null) {
            size = data.size();
        }

        Matrix4f matrix4f = new Matrix4f().identity();
        matrix4f.scale(0.5f);

        if (direction == Direction.DOWN || direction == Direction.UP) {
            matrix4f.setTranslation(0, -0.5f, 0);

            float ang = (float) java.lang.Math.toRadians(rotation + 180);
            double angleRadians = Mth.atan2(-Mth.sin(ang), Mth.cos(ang));
            matrix4f.rotate(Axis.YP.rotation((float) angleRadians).normalize());
            matrix4f.rotate(Axis.XP.rotationDegrees(-90));
            if (direction == Direction.DOWN) {
                matrix4f.rotate(Axis.XP.rotationDegrees(180));
                matrix4f.setTranslation(0, 0.5f, 0);
            }
        } else {
            double angleRadians = Mth.DEG_TO_RAD * direction.toYRot();
            Quaternionf rot = Axis.YP.rotation((float) angleRadians).conjugate().normalize();
            matrix4f.rotate(rot);
            matrix4f.setTranslation(new Vector3f(0.f, 0.f, -0.5f).rotate(rot));

        }

        itemDisplayElement.setDisplayWidth(size.x * 2.f);
        itemDisplayElement.setDisplayHeight(size.y * 2.f);

        itemDisplayElement.setTransformation(matrix4f);
        itemDisplayElement.setModelTransformation(data.properties().display);

        return itemDisplayElement;
    }

    public static void showBreakParticle(ServerLevel level, ItemStack stack, float x, float y, float z) {
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, z, 27, 0.125, 0.125, 0.125, 0.05);
    }
}
