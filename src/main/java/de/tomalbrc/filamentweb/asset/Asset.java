package de.tomalbrc.filamentweb.asset;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.util.Json;

import java.nio.file.Path;
import java.util.UUID;

public class Asset {
    public UUID uuid;
    public Path path;
    public Data<?> data;
    public String modid;
    public Class<?> type;

    public JsonElement schema;

    public JsonElement readJson() {
        return Json.GSON.toJsonTree(data);
    }

    public boolean writeJson(JsonElement json) {
        this.data = Json.GSON.fromJson(json, data.getClass());

        //if (path == null) return false;
        //try {
        //    String content = json != null ? new GsonBuilder().setPrettyPrinting().create().toJson(json) : "";
        //    Files.writeString(path, content, StandardCharsets.UTF_8);
        //    return true;
        //} catch (IOException e) {
        //    e.printStackTrace();
        //    return false;
        //}

        return true;
    }
}
