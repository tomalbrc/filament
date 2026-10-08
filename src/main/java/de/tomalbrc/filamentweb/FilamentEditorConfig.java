package de.tomalbrc.filamentweb;

import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FilamentEditorConfig {
    private FilamentEditorConfig() {}

    static final Path CONFIG_DIR = Constants.CONFIG_DIR.resolve("filament");
    static final Path CONFIG_FILE_PATH = CONFIG_DIR.resolve("web-editor.json");
    static final Path LEGACY_CONFIG_FILE_PATH = Constants.CONFIG_DIR.resolve("filament-editor.json");
    static FilamentEditorConfig instance;

    public boolean enabled = false;

    public boolean passwordLogin = false;
    public String defaultUser = "admin";
    public String defaultPassword = "hunter1";

    public String bindIp = "0.0.0.0";
    public int bindPort = 25599;
    public String externalAddress = "http://127.0.0.1:25599";

    public static FilamentEditorConfig getInstance() {
        if (instance == null) {
            migrateLegacyConfig();
            if (!load()) {
                save();
            }
        }
        return instance;
    }

    private static void migrateLegacyConfig() {
        if (!Files.exists(LEGACY_CONFIG_FILE_PATH) || Files.exists(CONFIG_FILE_PATH)) {
            return;
        }

        try {
            Files.createDirectories(CONFIG_DIR);

            FilamentEditorConfig legacy = Json.GSON.fromJson(
                    new FileReader(LEGACY_CONFIG_FILE_PATH.toFile()),
                    FilamentEditorConfig.class
            );

            if (legacy != null) {
                try (var stream = Files.newOutputStream(CONFIG_FILE_PATH)) {
                    stream.write(Json.GSON.toJson(legacy).getBytes(StandardCharsets.UTF_8));
                }
            }

            Files.deleteIfExists(LEGACY_CONFIG_FILE_PATH);
        } catch (IOException e) {
            // migration is best-effort; fall through to normal load path
        }
    }

    public static boolean load() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!Files.exists(CONFIG_FILE_PATH)) {
            instance = new FilamentEditorConfig();
            save();
            return true;
        }

        try {
            FilamentEditorConfig.instance = Json.GSON.fromJson(
                    new FileReader(CONFIG_FILE_PATH.toFile()),
                    FilamentEditorConfig.class
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (var stream = Files.newOutputStream(CONFIG_FILE_PATH)) {
            stream.write(Json.GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}