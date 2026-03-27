package de.tomalbrc.filamentweb.util;

import org.eclipse.jetty.util.UrlEncoded;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebPaths {
    public static String buildUrl(String base, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return base;
        }

        String query = params.entrySet().stream()
                .map(e -> UrlEncoded.encodeString(e.getKey()) + "=" + UrlEncoded.encodeString(e.getValue()))
                .collect(Collectors.joining("&"));

        return base + "?" + query;
    }

    public static String fragment(String op, String uuid, String path) {
        return fragment(op, uuid, path, null);
    }

    public static String fragment(String op, String uuid, String path, @Nullable String key) {
        Map<String, String> params = new HashMap<>();
        params.put("name", uuid);
        params.put("op", op);
        params.put("path", path);

        if (key != null) {
            params.put("key", key);
        }

        return buildUrl("/rest/fragment", params);
    }

    public static String files() {
        return "/rest/files";
    }

    public static String file(String string) {
        return "/rest/file?name=" + string;
    }
}
