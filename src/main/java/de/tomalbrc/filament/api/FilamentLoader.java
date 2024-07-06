package de.tomalbrc.filament.api;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.registry.filament.BlockRegistry;
import de.tomalbrc.filament.registry.filament.DecorationRegistry;
import de.tomalbrc.filament.registry.filament.ItemRegistry;
import de.tomalbrc.filament.registry.filament.ModelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.function.Consumer;

public class FilamentLoader {
    public static void loadBlocks(String modid) {
        search(modid, f -> {
            try {
                BlockRegistry.register(f);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, "filament/block");
    }

    public static void loadItems(String modid) {
        search(modid, f -> {
            try {
                ItemRegistry.register(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/item/");
    }

    public static void loadDecorations(String modid) {
        search(modid, f -> {
            try {
                DecorationRegistry.register(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/decoration/");
    }

    public static void loadModels(String modid, String namespace) {
        search(modid, f -> {
            try {
                ModelRegistry.registerAjModel(f, ResourceLocation.fromNamespaceAndPath(namespace, f.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/model/", ".ajmodel");

        search(modid, f -> {
            try {
                ModelRegistry.registerBbModel(f, ResourceLocation.fromNamespaceAndPath(namespace, f.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/model/", ".bbmodel");
    }

    private static void search(String modid, Consumer<InputStream> registry, String path) {
        search(modid, registry, path, ".json");
    }

    private static void search(String modid, Consumer<InputStream> registry, String path, String ext) {
        processJsonFilesInJar(modid, registry, path, ext);
    }

    private static void processJsonFilesInJar(String modid, Consumer<InputStream> consumer, String rootPath, String ext) {
        var container = FabricLoader.getInstance().getModContainer(modid);
        if (container.isPresent() && !container.get().getRootPaths().isEmpty()) {
            for (var rootPaths : container.get().getRootPaths()) {
                try (var str = Files.walk(rootPaths)) {
                    str.forEach(file -> {
                        try {
                            var name = file.getFileName().toString();
                            if (file.toAbsolutePath().toString().contains(rootPath) && name.endsWith(ext)) {
                                consumer.accept(Files.newInputStream(file));
                            }
                        } catch (Throwable ignored) {
                            ignored.printStackTrace();
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else {
            Filament.LOGGER.error("Not a jar");
        }
    }
}
