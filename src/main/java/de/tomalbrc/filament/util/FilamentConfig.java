package de.tomalbrc.filament.util;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.datafixer.DataFix;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilamentConfig {
    private FilamentConfig() {}

    static final Path CONFIG_DIR = Constants.CONFIG_DIR.resolve("filament");
    static final Path CONFIG_FILE_PATH = CONFIG_DIR.resolve("general.json");
    static final Path LEGACY_CONFIG_FILE_PATH = Constants.CONFIG_DIR.resolve("filament.json");
    static FilamentConfig instance;

    @SerializedName("debug")
    public boolean debug = false;

    @SerializedName("use_minimessage")
    public boolean minimessage = false;

    @SerializedName("commands")
    public boolean commands = true;

    @SerializedName("add_custom_menu_assets")
    public boolean addCustomMenuAssets = true;

    @SerializedName("prevent_adventure_mode_decoration_interaction")
    public boolean preventAdventureModeDecorationInteraction = true;

    @SerializedName("alternative_block_placement")
    public boolean alternativeBlockPlacement = false;

    @SerializedName("alternative_cosmetic_placement")
    public boolean alternativeCosmeticPlacement = false;

    @SerializedName("resourcepack_required")
    public boolean resourcepackRequired = true;

    @SerializedName("decoration_placement_previews")
    public boolean decorationPlacementPreviews = false;

    @SerializedName("preview_glow")
    public boolean previewGlow = true;

    @SerializedName("preview_glow_color")
    public int previewGlowColor = 0x00FF00;

    @SerializedName("preview_transparency")
    public float previewTransparency = 0.5f;

    @SerializedName("version")
    public Integer version;

    public static FilamentConfig getInstance() {
        if (instance == null) {
            migrateLegacyConfig();
            if (!load()) { // only save if file wasn't just created
                save(); // save since newer versions may contain new options, also removes old options
                if (instance.version == null) instance.version = 1;
            } else if (instance.version == null) {
                instance.version = DataFix.VERSION;
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

            FilamentConfig legacy = Json.GSON.fromJson(
                    new FileReader(LEGACY_CONFIG_FILE_PATH.toFile()),
                    FilamentConfig.class
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
            instance = new FilamentConfig();
            save();
            return true;
        }

        try {
            FilamentConfig.instance = Json.GSON.fromJson(
                    new FileReader(CONFIG_FILE_PATH.toFile()),
                    FilamentConfig.class
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
