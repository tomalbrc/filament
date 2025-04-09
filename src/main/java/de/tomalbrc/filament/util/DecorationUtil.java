package de.tomalbrc.filament.util;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;
import org.joml.*;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DecorationUtil {
    public static Int2ObjectOpenHashMap<ItemStack> VIRTUAL_ENTITY_PICK_MAP = new Int2ObjectOpenHashMap<>();

    public static void forEachRotated(List<DecorationData.BlockConfig> blockConfigs, BlockPos originBlockPos, float rotation, Consumer<BlockPos> consumer) {
        if (blockConfigs != null) {
            for (DecorationData.BlockConfig blockConfig : blockConfigs) {
                Vector3fc origin = blockConfig.origin();
                Vector3fc size = blockConfig.size();
                for (int x = 0; x < size.x(); x++) {
                    for (int y = 0; y < size.y(); y++) {
                        for (int z = 0; z < size.z(); z++) {
                            Vector3f pos = new Vector3f(x, y, z).add(origin);
                            Vector3f offset = pos.mul(rotation % 90 != 0 ? Math.sqrt(2) : 1);
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
        display.setItem(blockEntity.visualItemStack());
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
        itemDisplayElement.setItemDisplayContext(data.properties().display);

        return itemDisplayElement;
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
        if (!(itemStack.getItem() instanceof PolymerItem)) {
            return itemStack.copyWithCount(1);
        } else {
            return ((PolymerItem)itemStack.getItem()).getPolymerItemStack(itemStack, TooltipFlag.NORMAL, PacketContext.create(Filament.REGISTRY_ACCESS.compositeAccess()));
        }
    }
}
