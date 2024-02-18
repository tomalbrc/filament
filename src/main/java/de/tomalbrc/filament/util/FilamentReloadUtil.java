package de.tomalbrc.filament.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import java.util.List;

public class FilamentReloadUtil {
    private static final List<SimpleSynchronousResourceReloadListener> reloadListenerList = new ObjectArrayList<>();

    public static void registerEarlyReloadListener(SimpleSynchronousResourceReloadListener listener) {
        reloadListenerList.add(listener);
    }

    public static List<SimpleSynchronousResourceReloadListener> getReloadListeners() {
        return reloadListenerList;
    }
}
