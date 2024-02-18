package de.tomalbrc.filament.util;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.Map;

public class FilamentRPUtil {
    private static final Map<String, byte[]> data = new Object2ObjectLinkedOpenHashMap<>();

    public static void addData(String path, byte[] data) {
        FilamentRPUtil.data.put(path, data);
    }

    public static Map<String, byte[]> getData() {
        return data;
    }

    public static void clearData() {
        data.clear();
    }

    public static void registerCallback() {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            for (Map.Entry<String, byte[]> entry: FilamentRPUtil.getData().entrySet()) {
                resourcePackBuilder.addData(entry.getKey(), entry.getValue());
            }
            FilamentRPUtil.clearData();
        });
    }
}
