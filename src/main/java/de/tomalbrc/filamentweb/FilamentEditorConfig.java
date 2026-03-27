package de.tomalbrc.filamentweb;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FilamentEditorConfig {
    static Path CONFIG_FILE_PATH = Constants.CONFIG_DIR.resolve("filament-editor.json");
    static FilamentEditorConfig instance;

    @SerializedName("debug")
    public boolean debug = false;

    public boolean passwordLogin = false;
    public String defaultUser;
    public String defaultPassword;

    public String bindIp = "0.0.0.0";
    public int bindPort = 25599;
    public String externalAddress = "http://127.0.0.1:25599";

    public static FilamentEditorConfig getInstance() {
        if (instance == null) {
            if (!load()) {
                save();
            }
        }
        return instance;
    }
    public static boolean load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new FilamentEditorConfig();
            try {
                if (CONFIG_FILE_PATH.toFile().createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile());
                    stream.write(Json.GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
                    stream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            FilamentEditorConfig.instance = Json.GSON.fromJson(new FileReader(FilamentEditorConfig.CONFIG_FILE_PATH.toFile()), FilamentEditorConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static void save() {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(Json.GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
