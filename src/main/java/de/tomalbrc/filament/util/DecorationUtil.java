package de.tomalbrc.filament.util;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.decoration.util.DecorationItemDisplayElement;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DecorationUtil {
    public static Int2ObjectOpenHashMap<Supplier<ItemStack>> VIRTUAL_ENTITY_PICK_MAP = new Int2ObjectOpenHashMap<>();

    public static void forEachRotated(List<DecorationData.BlockConfig> blockConfigs, BlockPos originBlockPos, float rotation, Consumer<BlockPos> consumer) {
        if (blockConfigs != null) {
            for (DecorationData.BlockConfig blockConfig : blockConfigs) {
                Vector3fc origin = blockConfig.origin();
                Vector3fc size = blockConfig.size();
                for (int x = 0; x < size.x(); x++) {
                    for (int y = 0; y < size.y(); y++) {
                        for (int z = 0; z < size.z(); z++) {
                            Vector3f pos = new Vector3f(x, y, z).add(origin);
                            var hMul = rotation % 90 != 0 ? Math.sqrt(2) : 1;
                            Vector3f offset = pos.mul(hMul, 1, hMul);
                            offset.rotateY(Mth.DEG_TO_RAD * (rotation + (FilamentConfig.getInstance().alternativeBlockPlacement ? 0 : 180)));

                            BlockPos blockPos = new BlockPos(originBlockPos).offset(-Math.round(offset.x), Math.round(offset.y), Math.round(offset.z));
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

    public static InteractionElement decorationInteraction(DecorationData decorationData, Direction direction, @Nullable OnInteract onInteract) {
        InteractionElement element = new InteractionElement();
        if (decorationData != null && decorationData.size() != null) {
            element.setSize(decorationData.size().x, decorationData.size().y);
        } else {
            element.setSize(1.f, direction.equals(Direction.DOWN) ? 1.f : .5f); // default
        }

        element.setInteractionHandler(new VirtualElement.InteractionHandler() {
            @Override
            public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos) {
                ServerLevel serverLevel = player.level();
                BlockPos blockPos = BlockPos.containing(element.getHolder().getAttachment().getPos());
                InteractionResult result = InteractionResult.PASS;
                if (onInteract != null && serverLevel.mayInteract(player, blockPos)) {
                    result = onInteract.interact(player, hand, blockPos.getBottomCenter().add(pos));
                }

                if (!result.consumesAction()) DecorationUtil.defaultVirtualInteraction(player, hand, blockPos, pos, element.getHeight());
            }

            @Override
            public void attack(ServerPlayer player) {
                ServerLevel serverLevel = player.level();
                BlockPos blockPos = BlockPos.containing(element.getHolder().getAttachment().getPos());
                player.gameMode.handleBlockBreakAction(blockPos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP, serverLevel.getMaxY(), 0);
            }
        });

        var dirUnitVec = direction.getUnitVec3i();
        if (direction != Direction.DOWN && direction != Direction.UP) {
            element.setOffset(new Vec3(dirUnitVec.getX(), dirUnitVec.getY() + element.getHeight(), dirUnitVec.getZ()).multiply(1.f-element.getWidth(), 1, 1.f-element.getWidth()).scale(-0.5f));
        } else {
            element.setOffset(new Vec3(dirUnitVec.getX(), dirUnitVec.getY(), dirUnitVec.getZ()).add(0,  direction == Direction.UP ? -1.5f : 0.5f + ((1.f-element.getHeight())), 0));
        }

        return element;
    }

    public static ItemDisplayElement decorationItemDisplay(@NotNull DecorationData data, Direction direction, float rotation, ItemStack itemStack) {
        ItemDisplayElement element = new DecorationItemDisplayElement(itemStack);
        element.setInvisible(true);
        element.setTeleportDuration(0);

        if (data.properties().glow) {
            element.setBrightness(Brightness.FULL_BRIGHT);
        }

        Vector2f size = new Vector2f(1);
        if (data.hasBlocks()) {
            size = DecorationUtil.barrierDimensions(data.blocks(), rotation);
        } else if (data.size() != null) {
            size = data.size();
        }

        Matrix4f matrix4f = transform(data.properties().display, direction);
        if (data.properties().scale != null) matrix4f.scale(data.properties().scale);

        element.setYaw(rotation - (180));

        element.setDisplayWidth(size.x * 3.f);
        element.setDisplayHeight(size.y * 3.f);

        element.setTransformation(matrix4f);
        element.setItemDisplayContext(data.properties().display);

        return element;
    }

    private static Matrix4f transform(ItemDisplayContext context, Direction direction) {
        Matrix4f matrix4f = new Matrix4f();

        return switch (context) {
            case FIXED -> {
                matrix4f.scale(0.5f);

                if (direction == Direction.DOWN || direction == Direction.UP) {
                    matrix4f.setTranslation(0, direction == Direction.DOWN ? 0.5f : -0.5f, 0);

                    matrix4f.rotate(Axis.XP.rotationDegrees(-90));
                    if (direction == Direction.DOWN) {
                        matrix4f.rotate(Axis.XP.rotationDegrees(180));
                    }
                } else {
                    matrix4f.setTranslation(new Vector3f(0.f, 0.f, -0.5f));
                }

                yield matrix4f;
            }
            case HEAD -> {
                matrix4f.translate(0, 1.91f, 0);
                matrix4f.scaleLocal(0.64f);
                yield matrix4f;
            }
            default -> matrix4f;
        };
    }

    public static void showBreakParticle(ServerLevel level, ItemStack stack, float x, float y, float z) {
        showBreakParticle(level, Shapes.block(), stack, BlockPos.containing(x, y, z));
    }

    public static void showBreakParticleShaped(ServerLevel level, BlockPos blockPos, BlockState blockState, ItemStack stack) {
        if (blockState.isAir() || !blockState.shouldSpawnTerrainParticles()) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(level, blockPos);
        showBreakParticle(level, voxelShape, stack, blockPos);
    }

    public static void showBreakParticle(ServerLevel level, VoxelShape voxelShape, ItemStack stack, BlockPos blockPos) {
        double div = 0.25;
        List<Packet<? super ClientGamePacketListener>> packets = new ObjectArrayList<>();
        voxelShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double dx = Math.min(1.0, maxX - minX);
            double dy = Math.min(1.0, maxY - minY);
            double dz = Math.min(1.0, maxZ - minZ);
            int nx = Math.max(2, Mth.ceil(dx / div));
            int ny = Math.max(2, Mth.ceil(dy / div));
            int nz = Math.max(2, Mth.ceil(dz / div));
            for (int iX = 0; iX < nx; ++iX) {
                for (int iY = 0; iY < ny; ++iY) {
                    for (int iZ = 0; iZ < nz; ++iZ) {
                        double deltaX = (iX + 0.5) / nx;
                        double deltaY = (iY + 0.5) / ny;
                        double deltaZ = (iZ + 0.5) / nz;
                        double xOffset = deltaX * dx + minX;
                        double yOffset = deltaY * dy + minY;
                        double zOffset = deltaZ * dz + minZ;
                        packets.add(new ClientboundLevelParticlesPacket(new ItemParticleOption(ParticleTypes.ITEM, stack), true, false, blockPos.getX() + xOffset, blockPos.getY() + yOffset, blockPos.getZ() + zOffset, (float)deltaX - 0.5f, (float)deltaY - 0.5f, (float)deltaZ - 0.5f, 0.25f, 0));
                    }
                }
            }
        });

        if (!packets.isEmpty()) {
            ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
            for (ServerPlayer player : level.players()) {
                if (player.position().distanceTo(blockPos.getCenter()) < 512) {
                    player.connection.send(bundlePacket);
                }
            }
        }
    }

    public static ItemStack placementAdjustedItem(ItemStack itemStack, ItemResource itemResource, boolean wall, boolean ceiling) {
        var converted = clientsideItem(itemStack);

        // TODO: this should be a behaviour
        if (wall && itemResource.getModels().containsKey("wall")) {
            converted.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(ImmutableList.of(), ImmutableList.of(), ImmutableList.of("wall"), ImmutableList.of()));
            return converted;
        }

        if (ceiling && itemResource.getModels().containsKey("ceiling")) {
            converted.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(ImmutableList.of(), ImmutableList.of(), ImmutableList.of("ceiling"), ImmutableList.of()));
            return converted;
        }

        if (itemResource.getModels().containsKey("floor")) {
            converted.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(ImmutableList.of(), ImmutableList.of(), ImmutableList.of("floor"), ImmutableList.of()));
            return converted;
        }

        converted.remove(DataComponents.CUSTOM_NAME);
        return converted;
    }

    public static ItemStack clientsideItem(ItemStack itemStack) {
        if (itemStack == null)
            return ItemStack.EMPTY;

        if (!(itemStack.getItem() instanceof PolymerItem)) {
            return itemStack.copyWithCount(1);
        } else {
            return ((PolymerItem)itemStack.getItem()).getPolymerItemStack(itemStack, TooltipFlag.NORMAL, PacketContext.create(Filament.REGISTRY_ACCESS.compositeAccess()));
        }
    }

    public static void setupElements(@NotNull FilamentDecorationHolder holder, @NotNull DecorationData data, @NotNull Direction direction, float rotation, @NotNull ItemStack itemStack, @Nullable OnInteract onInteract) {
        boolean addDisplay = !holder.isAnimated();

        if (data.hasBlocks() && addDisplay) {
            holder.addElement(DecorationUtil.decorationItemDisplay(data, direction, rotation, itemStack));
        } else if (data.size() != null) {
            if (addDisplay)
                holder.addElement(DecorationUtil.decorationItemDisplay(data, direction, rotation, itemStack));
            holder.addElement(DecorationUtil.decorationInteraction(data, direction, onInteract));
        } else {
            if (data.itemFrame() == Boolean.TRUE && addDisplay) {
                ItemFrameElement itemFrameElement = new ItemFrameElement(data, direction, Util.SEGMENTED_ANGLE8.fromDegrees(rotation), itemStack, onInteract);
                holder.addElement(itemFrameElement);
            } else if (!data.hasBlocks()) {
                // Just using display+interaction again with 1.0 width, 0.5 height
                if (addDisplay) holder.addElement(DecorationUtil.decorationItemDisplay(data, direction, rotation, itemStack));
                holder.addElement(DecorationUtil.decorationInteraction(data, direction, onInteract));
            }
        }
    }

    public static void defaultVirtualInteraction(ServerPlayer player, InteractionHand hand, BlockPos blockPos, Vec3 position, float height) {
        player.connection.handleUseItemOn(new ServerboundUseItemOnPacket(hand, new BlockHitResult(blockPos.getCenter().add(position), Direction.getApproximateNearest(position.multiply(1, 1.f/height * 0.5, 1)), blockPos, false), 0));
    }

    @FunctionalInterface
    public interface OnInteract {
        InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 pos);
    }
}
