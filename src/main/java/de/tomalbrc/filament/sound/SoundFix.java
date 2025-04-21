package de.tomalbrc.filament.sound;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundEntry;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class SoundFix {
    public static final Reference2ObjectOpenHashMap<SoundType, SoundType> REMIXES = new Reference2ObjectOpenHashMap<>();
    public static final ReferenceOpenHashSet<SoundType> SOUND_TYPES = new ReferenceOpenHashSet<>();

    static String PATH = "assets/minecraft/sounds.json";
    static String VERSION = "1.21.5";

    public static void init() {
        Path outputPath = Path.of("polymer/sounds-" + VERSION + ".json");
        byte[] data = null;
        try {
            if (Files.exists(outputPath)) {
                data = Files.readAllBytes(outputPath);
            } else {
                data = MinecraftAssetFetcher.fetchSoundsJsonForVersion("1.21.5");
            }

            if (data == null)
                return;

            Files.write(outputPath, data);
        } catch (Exception ignored) {}

        if (data != null) {
            SoundsAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(data)))).ifSuccess(pair -> {
                SoundsAsset soundsAsset = pair.getFirst();
                addData(soundsAsset);
            });
        }
    }

    private static void addData(SoundsAsset vanillaSounds) {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            if (!REMIXES.isEmpty())
                return;

            SoundEntry empty = SoundEntry.builder().replace(true).build();
            SoundsAsset newSounds = new SoundsAsset(new Object2ObjectArrayMap<>());
            for (SoundType soundType : SOUND_TYPES) {
                newSounds.sounds().put(soundType.getBreakSound().location().getPath(), empty);
                newSounds.sounds().put(soundType.getStepSound().location().getPath(), empty);
                newSounds.sounds().put(soundType.getHitSound().location().getPath(), empty);
                newSounds.sounds().put(soundType.getFallSound().location().getPath(), empty);

                ResourceLocation breakId = serversideId(soundType.getBreakSound().location());
                ResourceLocation stepId = serversideId(soundType.getStepSound().location());
                ResourceLocation hitId = serversideId(soundType.getHitSound().location());
                ResourceLocation fallId = serversideId(soundType.getFallSound().location());

                newSounds.sounds().put(breakId.getPath(), vanillaSounds.sounds().get(soundType.getBreakSound().location().getPath()));
                newSounds.sounds().put(stepId.getPath(), vanillaSounds.sounds().get(soundType.getStepSound().location().getPath()));
                newSounds.sounds().put(hitId.getPath(), vanillaSounds.sounds().get(soundType.getHitSound().location().getPath()));
                newSounds.sounds().put(fallId.getPath(), vanillaSounds.sounds().get(soundType.getFallSound().location().getPath()));

                REMIXES.put(soundType, new SoundType(
                        soundType.getVolume(),
                        soundType.getPitch(),
                        SoundEvent.createVariableRangeEvent(breakId),
                        SoundEvent.createVariableRangeEvent(stepId),
                        SoundEvent.createVariableRangeEvent(soundType.getPlaceSound().location()), // no need to mess with place sounds (at least with filament)
                        SoundEvent.createVariableRangeEvent(hitId),
                        SoundEvent.createVariableRangeEvent(fallId)
                ));
            }
            resourcePackBuilder.addData(PATH, newSounds.toBytes());
        });
    }

    private static ResourceLocation serversideId(ResourceLocation resourceLocation) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath() + ".serverside");
    }
}
