package de.tomalbrc.filament.util;

import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.config.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static final SegmentedAnglePrecision SEGMENTED_ANGLE8 = new SegmentedAnglePrecision(3); // 3 bits precision = 8

    public static void handleBlockPlaceEffects(ServerPlayer player, InteractionHand hand, BlockPos pos, SoundType type) {
        player.swing(hand, true);
        Util.playBlockPlaceSound(player, pos, type);
    }

    public static void playBlockPlaceSound(ServerPlayer player, BlockPos pos, SoundType type) {
        player.connection.send(new ClientboundSoundPacket(
                Holder.direct(type.getPlaceSound()),
                SoundSource.BLOCKS,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                (type.getVolume() + 1.0F) / 2.0F,
                type.getPitch() * 0.8F,
                player.level().getRandom().nextLong()
        ));
    }

    public static Optional<Integer> validateAndConvertHexColor(String hexColor) {
        // Regular expression pattern to match hex color strings
        Pattern hexColorPattern = Pattern.compile("^#?([A-Fa-f0-9]{6})$|^0x([A-Fa-f0-9]{6})$");

        Matcher matcher = hexColorPattern.matcher(hexColor);

        if (matcher.matches()) {
            String hexDigits = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            int intValue = Integer.parseInt(hexDigits, 16);
            return Optional.of(intValue);
        } else {
            Filament.LOGGER.warn("Invalid hex color format");
            return Optional.empty();
        }
    }

    public static void forEachRotated(List<DecorationData.BlockConfig> blockConfigs, BlockPos originBlockPos, float rotation, Consumer<BlockPos> consumer) {
        if (blockConfigs != null) {
            blockConfigs.forEach(blockConfig -> {
                Vector3fc origin = blockConfig.origin();
                Vector3fc size = blockConfig.size();
                for (int x = 0; x < size.x(); x++) {
                    for (int y = 0; y < size.y(); y++) {
                        for (int z = 0; z < size.z(); z++) {
                            Vector3f pos = new Vector3f(x, y, z).add(origin);
                            Vector3f offset = pos.mul(rotation % 90 != 0 ? Math.sqrt(2) : 1);
                            offset.rotateY(Math.toRadians(rotation + 180));

                            BlockPos blockPos = new BlockPos(originBlockPos).offset(-Math.round(offset.x), Math.round(offset.y), Math.round(offset.z));
                            consumer.accept(blockPos);
                        }
                    }
                }
            });
        }
    }

    public static Vector2f barrierDimensions(List<DecorationData.BlockConfig> blockConfigs, float rotation) {
        if (!blockConfigs.isEmpty()) {
            List<BlockPos> posList = new ArrayList<>();
            Util.forEachRotated(blockConfigs, BlockPos.ZERO, rotation, blockPos2 -> {
                posList.add(blockPos2);
            });
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
                blockEntity.interact(player, hand, blockEntity.getBlockPos().getCenter().subtract(0, 0.5f, 0).add(pos));
            }

            @Override
            public void attack(ServerPlayer player) {
                blockEntity.destroyStructure(true);
            }
        });

        if (blockEntity.getDecorationData() != null && blockEntity.getDecorationData().size() != null) {
            if (!(blockEntity.getDirection().equals(Direction.DOWN) || blockEntity.getDirection().equals(Direction.UP))) {
                element.setSize(blockEntity.getDecorationData().size().y, blockEntity.getDecorationData().size().x);
            } else {
                element.setSize(blockEntity.getDecorationData().size().x, (blockEntity.getDirection().equals(Direction.DOWN) ? -1.f : 1.f) * blockEntity.getDecorationData().size().y);
            }
        } else {
            // shouldn't really happen
            element.setSize(1.f, blockEntity.getDirection().equals(Direction.DOWN) ? -0.5f : 0.5f);
        }

        element.setOffset(new Vec3(0, -0.49f, 0));

        return element;
    }

    public static ItemDisplayElement decorationItemDisplay(DecorationBlockEntity entity) {
        ItemDisplayElement itemDisplayElement = new ItemDisplayElement(entity.getItem());

        if (entity.getDecorationData() != null && entity.getDecorationData().properties() != null && entity.getDecorationData().properties().glow) {
            itemDisplayElement.setBrightness(Brightness.FULL_BRIGHT);
        }

        Vector2f size = new Vector2f(1);
        if (entity.getDecorationData().blocks() != null) {
            size = Util.barrierDimensions(entity.getDecorationData().blocks(), entity.getVisualRotationYInDegrees());
        } else if (entity.getDecorationData().size() != null) {
            size = entity.getDecorationData().size();
        }

        Matrix4f matrix4f = new Matrix4f().identity();
        matrix4f.scale(0.5f);

        if (entity.getDirection() == Direction.DOWN || entity.getDirection() == Direction.UP) {
            matrix4f.setTranslation(0, -0.5f, 0);

            float ang = (float) java.lang.Math.toRadians(entity.getVisualRotationYInDegrees()+180);
            double angleRadians = Mth.atan2(-Mth.sin(ang), Mth.cos(ang));
            matrix4f.rotate(Axis.YP.rotation((float) angleRadians).normalize());
            matrix4f.rotate(Axis.XP.rotationDegrees(-90));
        } else {
            matrix4f.setTranslation(0, 0, -0.5f);

            Vector3f e = entity.getDirection().getRotation().getEulerAnglesYXZ(new Vector3f());
            matrix4f.rotate(Axis.YP.rotation(e.y).normalize());
        }

        DecorationData fd = entity.getDecorationData();
        if (fd != null && fd.size() != null) {
            itemDisplayElement.setDisplayWidth(fd.size().get(0)*1.5f);
            itemDisplayElement.setDisplayHeight(fd.size().get(1)*1.5f);
        } else {
            itemDisplayElement.setDisplayWidth(Math.min(size.x, 1.f)*2);
            itemDisplayElement.setDisplayHeight(Math.min(size.y, 1.f)*2);
        }

        itemDisplayElement.setTransformation(matrix4f);
        itemDisplayElement.setModelTransformation(ItemDisplayContext.FIXED);

        return itemDisplayElement;
    }

    @Nullable
    public static ItemEntity spawnAtLocation(Level level, Vec3 pos, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        } else if (level.isClientSide) {
            return null;
        } else {
            ItemEntity itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
            return itemEntity;
        }
    }
}
