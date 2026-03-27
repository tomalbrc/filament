package de.tomalbrc.filamentweb.service;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filamentweb.util.JsonPathUtil;
import de.tomalbrc.filamentweb.SchemaFormBuilder;
import de.tomalbrc.filamentweb.util.SchemaUtil;
import de.tomalbrc.filamentweb.asset.Asset;
import de.tomalbrc.filamentweb.asset.AssetStore;
import j2html.tags.ContainerTag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static j2html.TagCreator.div;
import static j2html.TagCreator.input;

public class ReadFileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");

        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'name' parameter");
            return;
        }

        var pane = SchemaFormBuilder.renderPane(name);
        resp.getWriter().write(pane.render());

        var currentUuidField = input()
                .withType("hidden")
                .withId("current-file-uuid")
                .withName("uuid")
                .withValue(name)
                .attr("hx-swap-oob", "true");

        resp.getWriter().write(currentUuidField.render());
    }

    public static ContainerTag<?> renderOob(String uuid) {
        return div().withId("editor-pane").attr("hx-swap-oob", "true").with(
                SchemaFormBuilder.renderPane(uuid)
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'name' parameter");
            return;
        }

        Asset asset;
        try { asset = AssetStore.getAsset(UUID.fromString(name)); } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid file identifier");
            return;
        }

        if (asset == null || asset.path == null || !Files.exists(asset.path) || !Files.isRegularFile(asset.path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File not found");
            return;
        }

        // get raw json if any
        String raw = req.getParameter("root");
        if (raw == null) raw = req.getParameter("content");
        String contentToSave;

        if (raw != null) {
            contentToSave = raw;
        } else {
            Map<String, String[]> paramMap = new HashMap<>(req.getParameterMap());
            paramMap.remove("name"); // don't include the asset id

            try {
                JsonElement built = buildJsonFromParams(paramMap);
                Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
                contentToSave = gson.toJson(built);
            } catch (Exception e) {
                Filament.LOGGER.warn("Failed to reconstruct JSON from parameters for {}", name, e);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Failed to reconstruct JSON from form parameters: " + e.getMessage());
                return;
            }
        }

        try {
            Path target = asset.path;
            Path tmp = Files.createTempFile(target.getParent(), "tmp-", ".tmp");
            Files.writeString(tmp, contentToSave, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            try { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING); }

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("text/html; charset=UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(div("Saved").withClass("alert alert-success mb-0").render());
        } catch (IOException e) {
            Filament.LOGGER.error("Unable to save file {}", asset.path, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Unable to save file");
        }
    }

    private static JsonElement buildJsonFromParams(Map<String, String[]> params) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, String[]> e : params.entrySet()) {
            String rawKey = e.getKey();
            String[] values = e.getValue();
            if (values == null) continue;

            for (String v : values) {
                JsonElement coerced = SchemaUtil.coercePrimitive(v);
                JsonPathUtil.setValueAtPath(root, rawKey, coerced);
            }
        }
        return root;
    }
}