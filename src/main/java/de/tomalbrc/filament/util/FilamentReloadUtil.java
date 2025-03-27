package de.tomalbrc.filament.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class FilamentReloadUtil {
    private static final List<FilamentSynchronousResourceReloadListener> reloadListenerList = new ObjectArrayList<>();

    public static void registerEarlyReloadListener(FilamentSynchronousResourceReloadListener listener) {
        reloadListenerList.add(listener);
    }

    public static List<FilamentSynchronousResourceReloadListener> getReloadListeners() {
        return reloadListenerList;
    }
}
