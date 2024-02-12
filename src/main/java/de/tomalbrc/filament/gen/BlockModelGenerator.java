package de.tomalbrc.filament.gen;

import com.google.gson.JsonObject;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.mixin.ModelTemplateAccessor;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
