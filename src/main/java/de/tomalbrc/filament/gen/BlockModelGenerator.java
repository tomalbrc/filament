package de.tomalbrc.filament.gen;

import com.google.gson.JsonObject;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class BlockModelGenerator {
    public static String generate(ModelTemplate template, Map<TextureSlot, ResourceLocation> stateMap) {
        JsonObject j = template.createBaseTemplate(null, stateMap);
        return j.toString();
    }


    static byte[] generateBlock() {
        return null;
    }
    static byte[] generatePillar() {
        return null;
    }
    static byte[] directional() {
        return null;
    }

    static byte[] horizontalDirectional() {
        return null;
    }
}
