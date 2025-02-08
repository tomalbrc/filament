package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.Data;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
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