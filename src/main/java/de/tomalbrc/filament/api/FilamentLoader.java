package de.tomalbrc.filament.api;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import de.tomalbrc.filament.registry.ModelRegistry;
import de.tomalbrc.filament.util.Json;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class FilamentLoader {
    public static void loadBlocks(String modid) {
        Behaviours.register();
        search(modid, f -> {
            try {
                if (f.endsWith(".yaml") || f.endsWith(".yml")) {
                    var list = Json.yamlToJson(Files.newInputStream(f));
                    for (InputStream stream : list) {
                        BlockRegistry.register(stream);
                    }
                }
                else {
                    BlockRegistry.register(Json.camelToSnakeCase(Files.newInputStream(f)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/block");
    }

    public static void loadItems(String modid) {
        Behaviours.register();
        search(modid, f -> {
            try {
                if (f.endsWith(".yaml") || f.endsWith(".yml")) {
                    var list = Json.yamlToJson(Files.newInputStream(f));
                    for (InputStream stream : list) {
                        ItemRegistry.register(stream);
                    }
                }
                else {
                    ItemRegistry.register(Json.camelToSnakeCase(Files.newInputStream(f)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/item/");
    }

    public static void loadDecorations(String modid) {
        Behaviours.register();
        search(modid, f -> {
            try {
                if (f.endsWith(".yaml") || f.endsWith(".yml")) {
                    var list = Json.yamlToJson(Files.newInputStream(f));
                    for (InputStream stream : list) {
                        DecorationRegistry.register(stream);
                    }
                }
                else {
                    DecorationRegistry.register(Json.camelToSnakeCase(Files.newInputStream(f)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/decoration/");
    }

    public static void loadModels(String modid, String namespace) {
        Behaviours.register();
        search(modid, f -> {
            try {
                if (f.getFileName() != null)
                    ModelRegistry.registerAjModel(Files.newInputStream(f), ResourceLocation.fromNamespaceAndPath(namespace, f.getFileName().toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/model/", ".ajmodel");

        search(modid, f -> {
            try {
                if (f.getFileName() != null)
                    ModelRegistry.registerBbModel(Files.newInputStream(f), ResourceLocation.fromNamespaceAndPath(namespace, f.getFileName().toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/model/", ".bbmodel");

        search(modid, f -> {
            try {
                if (f.getFileName() != null)
                    ModelRegistry.registerAjBlueprintModel(Files.newInputStream(f), ResourceLocation.fromNamespaceAndPath(namespace, f.getFileName().toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/model/", ".ajblueprint");
    }

    private static void search(String modid, Consumer<Path> registry, String path) {
        search(modid, registry, path, ".json");
        search(modid, registry, path, ".yaml");
        search(modid, registry, path, ".yml");
    }

    private static void search(String modid, Consumer<Path> registry, String path, String ext) {
        processJsonFilesInJar(modid, registry, path, ext);
    }

    private static void processJsonFilesInJar(String modid, Consumer<Path> consumer, String rootPath, String ext) {
        var container = FabricLoader.getInstance().getModContainer(modid);
        if (container.isPresent() && !container.get().getRootPaths().isEmpty()) {
            for (var rootPaths : container.get().getRootPaths()) {
                try (var str = Files.walk(rootPaths)) {
                    str.forEach(file -> {
                        try {
                            var name = file.getFileName().toString();
                            if (file.toAbsolutePath().toString().contains(rootPath) && name.endsWith(ext)) {
                                consumer.accept(file);
                            }
                        } catch (Throwable ignored) {
                            //ignored.printStackTrace();
                        }
                    });
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        } else {
            Filament.LOGGER.error("Not a jar");
        }
    }
}
