package de.tomalbrc.filamentweb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class JsonPathUtil {
    public static JsonElement getElementAtPath(JsonElement root, String path) {
        if (root == null) {
            return null;
        }
        if (path == null || path.isBlank()) {
            return root;
        }

        List<String> segments = JsonPathUtil.parsePath(path);
        JsonElement current = root;

        for (String segment : segments) {
            if (current == null || current.isJsonNull()) {
                return null;
            }

            if (current.isJsonObject()) {
                JsonObject obj = current.getAsJsonObject();
                current = obj.has(segment) ? obj.get(segment) : null;
                continue;
            }
            if (current.isJsonArray() && isInteger(segment)) {
                JsonArray arr = current.getAsJsonArray();
                int idx = Integer.parseInt(segment);
                current = idx >= 0 && idx < arr.size() ? arr.get(idx) : null;
                continue;
            }
            return null;
        }

        return current == null ? JsonNull.INSTANCE : current;
    }

    public static void setValueAtPath(JsonElement root, String path, JsonElement value) {
        if (root == null || path == null || path.isBlank()) {
            return;
        }

        List<String> segments = JsonPathUtil.parsePath(path);
        if (segments.isEmpty()) {
            return;
        }

        if (segments.size() == 1 && root.isJsonObject()) {
            root.getAsJsonObject().add(segments.getFirst(), value == null ? JsonNull.INSTANCE : value.deepCopy());
            return;
        }

        JsonElement current = root;

        for (int i = 0; i < segments.size() - 1; i++) {
            String segment = segments.get(i);
            String next = segments.get(i + 1);
            current = descendOrCreate(current, segment, next);
            if (current == null) {
                return;
            }
        }

        String last = segments.getLast();
        if (current.isJsonObject()) {
            current.getAsJsonObject().add(last, value == null ? JsonNull.INSTANCE : value.deepCopy());
        } else if (current.isJsonArray() && isInteger(last)) {
            JsonArray arr = current.getAsJsonArray();
            int idx = Integer.parseInt(last);
            while (arr.size() < idx) {
                arr.add(JsonNull.INSTANCE);
            }
            if (idx == arr.size()) {
                arr.add(value == null ? JsonNull.INSTANCE : value.deepCopy());
            } else {
                arr.set(idx, value == null ? JsonNull.INSTANCE : value.deepCopy());
            }
        }
    }

    private static JsonElement descendOrCreate(JsonElement current, String segment, String next) {
        if (current == null) {
            return null;
        }

        if (current.isJsonObject()) {
            JsonObject obj = current.getAsJsonObject();
            if (!obj.has(segment) || obj.get(segment).isJsonNull()) {
                obj.add(segment, isInteger(next) ? new JsonArray() : new JsonObject());
            }
            return obj.get(segment);
        }

        if (current.isJsonArray() && isInteger(segment)) {
            JsonArray arr = current.getAsJsonArray();
            int idx = Integer.parseInt(segment);

            while (arr.size() <= idx) {
                arr.add(JsonNull.INSTANCE);
            }

            JsonElement child = arr.get(idx);
            if (child == null || child.isJsonNull()) {
                child = isInteger(next) ? new JsonArray() : new JsonObject();
                arr.set(idx, child);
            }
            return child;
        }

        return null;
    }

    public static List<String> parsePath(String path) {
        List<String> segments = new ArrayList<>();
        if (path == null || path.isBlank()) {
            return segments;
        }

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '[' || c == ']') {
                if (!current.isEmpty()) {
                    segments.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            segments.add(current.toString());
        }

        return segments;
    }

    public static boolean isInteger(String s) {
        return s != null && s.matches("-?\\d+");
    }
}
