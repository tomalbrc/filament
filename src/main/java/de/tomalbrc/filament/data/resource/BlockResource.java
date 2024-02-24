package de.tomalbrc.filament.data.resource;

import de.tomalbrc.filament.data.BlockData;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record BlockResource(Map<String, ResourceLocation> models,
                           Map<String, ResourceLocation> textures,
                           Map<String, ResourceLocation> vanilla) {

    public boolean couldGenerate() {
        return this.textures != null;
    }

    public boolean hasValidModels(BlockData.BlockType type) {
        if (models == null || type == null)
            return false;

        boolean horizontal_directional =
                models.containsKey("north") &&
                models.containsKey("south") &&
                models.containsKey("west") &&
                models.containsKey("east");

        switch (type) {
            case block -> {
                return models.containsKey("default");
            }
            case column -> {
                return models.containsKey("x") && models.containsKey("y") && models.containsKey("z");
            }
            case directional -> {
                return models.containsKey("up") && models.containsKey("down") && horizontal_directional;
            }
            case horizontal_directional -> {
                return horizontal_directional;
            }
        }

        return false;
    }

    public List<ModelTemplate> getTemplates(BlockData.BlockType blockType) {
        switch (blockType) {
            case block -> {
                return List.of(ModelTemplates.CUBE_ALL);
            }
            case column -> {
                return List.of(ModelTemplates.CUBE_COLUMN_UV_LOCKED_X, ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y, ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z);
            }
            case directional -> {
                return List.of(ModelTemplates.CUBE);
            }
            case horizontal_directional -> {
                return List.of();
            }
        }

        return null;
    }

    public boolean hasValidTextures(BlockData.BlockType type) {
        if (models == null || type == null)
            return false;

        boolean horizontal_directional =
                models.containsKey("north") &&
                        models.containsKey("south") &&
                        models.containsKey("west") &&
                        models.containsKey("east");

        switch (type) {
            case block -> {
                return models.containsKey("default");
            }
            case column -> {
                return models.containsKey("top") && models.containsKey("side") && models.containsKey("bottom");
            }
            case directional, horizontal_directional -> {
                return models.containsKey("front") && models.containsKey("top") && models.containsKey("side");
            }
        }

        return false;
    }
}
