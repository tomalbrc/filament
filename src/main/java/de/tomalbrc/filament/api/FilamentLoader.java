package de.tomalbrc.filament.api;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import de.tomalbrc.filament.registry.ModelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class FilamentLoader {
    public static void loadBlocks(String modid) {
        Behaviours.init();
        search(modid, f -> {
            try {
                BlockRegistry.register(Files.newInputStream(f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/block");
    }

    public static void loadItems(String modid) {
        Behaviours.init();
        search(modid, f -> {
            try {
                ItemRegistry.register(Files.newInputStream(f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/item/");
    }

    public static void loadDecorations(String modid) {
        Behaviours.init();
        search(modid, f -> {
            try {
                DecorationRegistry.register(Files.newInputStream(f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "filament/decoration/");
    }

    public static void loadModels(String modid, String namespace) {
        Behaviours.init();
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
    }

    private static void search(String modid, Consumer<Path> registry, String path) {
        search(modid, registry, path, ".json");
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
