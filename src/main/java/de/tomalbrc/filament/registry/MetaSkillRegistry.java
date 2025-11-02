package de.tomalbrc.filament.registry;

import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.entity.skill.meta.MetaSkill;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MetaSkillRegistry {
    private static final Map<ResourceLocation, MetaSkill> types = new HashMap<>();

    public static void register(InputStream inputStream) throws IOException {
        var element = JsonParser.parseReader(new InputStreamReader(inputStream));
        MetaSkill skill = Json.GSON.fromJson(element, MetaSkill.class);
        register(skill);
    }

    static public void register(MetaSkill data) {
        types.put(data.id(), data);
        FilamentRegistrationEvents.METASKILL.invoker().registered(data);
    }

    public static MetaSkill get(ResourceLocation resourceLocation) {
        return types.get(resourceLocation);
    }

    public static class MetaSkillReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "metaskill");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/metaskill", null, resourceManager, (id, inputStream) -> {
                try {
                    MetaSkillRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load MetaSkill resource \"{}\".", id);
                }
            });
        }
    }
}
