package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Axis;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static final SegmentedAnglePrecision SEGMENTED_ANGLE8 = new SegmentedAnglePrecision(3); // 3 bits precision = 8

    public static void handleBoneMealEffects(ServerLevel level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getCenter().x, blockPos.getCenter().y, blockPos.getCenter().z, 15, 0.25, 0.25, 0.25, 0.15);
    }

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

    public static void showBreakParticle(ServerLevel level, ItemStack stack, float x, float y, float z) {
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, z, 27, 0.125, 0.125, 0.125, 0.05);
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
            Filament.LOGGER.warn("Invalid hex color formats");
            return Optional.empty();
        }
    }

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
                            offset.rotateY(Math.toRadians(rotation + 180));

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
            Util.forEachRotated(blockConfigs, BlockPos.ZERO, rotation, posList::add);
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
                if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE)
                    blockEntity.interact(player, hand, blockEntity.getBlockPos().getCenter().subtract(0, 0.5f, 0).add(pos));
            }

            @Override
            public void attack(ServerPlayer player) {
                if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                    blockEntity.destroyStructure(player != null && !player.isCreative());
                }
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
            element.setOffset(new Vec3(q.getX(), q.getY(), q.getZ()).multiply(1, blockEntity.getDirection() == Direction.DOWN ? 1.f-element.getHeight()/2.f : -1, 1).scale(0.5f));
        }

        return element;
    }

    public static InteractionElement decorationInteraction(DecorationBlockEntity decorationBlockEntity, DecorationData decorationData) {
        InteractionElement element = new InteractionElement();
        element.setHandler(new VirtualElement.InteractionHandler() {
            @Override
            public void attack(ServerPlayer player) {
                if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                    element.getHolder().getAttachment().getWorld().destroyBlock(BlockPos.containing(element.getHolder().getAttachment().getPos()), false);
                }
            }
        });
        element.setSize(1.f, 1.f);

        if (decorationData.size() != null) {
            element.setSize(decorationData.size().x, decorationData.size().y);
        } else {
            element.setSize(1.f, 1.f);
        }

        if (decorationBlockEntity != null) {
            var q = decorationBlockEntity.getDirection().getUnitVec3i();
            if (decorationBlockEntity.getDirection() != Direction.DOWN && decorationBlockEntity.getDirection() != Direction.UP) {
                element.setOffset(new Vec3(q.getX(), q.getY() + element.getHeight(), q.getZ()).multiply(1.f-element.getWidth(), 1, 1.f-element.getWidth()).scale(-0.5f));
            } else {
                element.setOffset(new Vec3(q.getX(), q.getY(), q.getZ()).multiply(1, decorationBlockEntity.getDirection() == Direction.DOWN ? 1.f-element.getHeight()/2.f : -1, 1).scale(0.5f));
            }
        }

        return element;
    }

    public static ItemDisplayElement decorationItemDisplay(DecorationBlockEntity blockEntity) {
        ItemDisplayElement display = decorationItemDisplay(blockEntity.getDecorationData(), blockEntity.getDirection(), blockEntity.getVisualRotationYInDegrees());
        display.setItem(blockEntity.getItem().copyWithCount(1));
        return display;
    }

    public static ItemDisplayElement decorationItemDisplay(DecorationData data, Direction direction, float rotation) {
        ItemDisplayElement itemDisplayElement = new ItemDisplayElement(BuiltInRegistries.ITEM.get(data.id()).orElseThrow().value());
        itemDisplayElement.setTeleportDuration(1);

        if (data != null && data.properties().glow) {
            itemDisplayElement.setBrightness(Brightness.FULL_BRIGHT);
        }

        Vector2f size = new Vector2f(1);
        if (data.hasBlocks()) {
            size = Util.barrierDimensions(data.blocks(), rotation);
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

        itemDisplayElement.setTransformation(matrix4f);
        itemDisplayElement.setModelTransformation(data.properties().display);

        return itemDisplayElement;
    }

    public static void spawnAtLocation(Level level, Vec3 pos, ItemStack itemStack) {
        if (!itemStack.isEmpty() && !level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public static void loadDatapackContents(ResourceManager resourceManager) {
        ((RegistryUnfreezer) BuiltInRegistries.BLOCK).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.ITEM).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$unfreeze();
        ((RegistryUnfreezer) BuiltInRegistries.CREATIVE_MODE_TAB).filament$unfreeze();

        for (SimpleSynchronousResourceReloadListener listener : FilamentReloadUtil.getReloadListeners()) {
            listener.onResourceManagerReload(resourceManager);
        }

        ((RegistryUnfreezer)BuiltInRegistries.BLOCK).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.ITEM).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$freeze();
        ((RegistryUnfreezer)BuiltInRegistries.CREATIVE_MODE_TAB).filament$freeze();
    }

    public static void damageAndBreak(int i, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot slot) {
        int newDamage = itemStack.getDamageValue() + i;
        itemStack.setDamageValue(newDamage);

        if (newDamage >= itemStack.getMaxDamage()) {
            Item item = itemStack.getItem();
            itemStack.shrink(1);
            livingEntity.onEquippedItemBroken(item, slot);
        }
    }

    public static void handleComponentsCustom(JsonElement element, Data data) {
        if (element.getAsJsonObject().has("components")) {
            JsonObject comp = element.getAsJsonObject().get("components").getAsJsonObject();
            if (comp.has("minecraft:jukebox_playable")) {
                data.putAdditional(DataComponents.JUKEBOX_PLAYABLE, comp.getAsJsonObject("minecraft:jukebox_playable"));
            }
            if (comp.has("jukebox_playable")) {
                data.putAdditional(DataComponents.JUKEBOX_PLAYABLE, comp.getAsJsonObject("jukebox_playable"));
            }
        }
    }
}