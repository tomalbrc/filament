package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.mixin.accessor.PackAccessor;
import de.tomalbrc.filament.mixin.accessor.PathResourcesSupplierAccessor;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filamentweb.asset.Asset;
import de.tomalbrc.filamentweb.asset.AssetStore;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static j2html.TagCreator.*;

public class ActionServlet extends HttpServlet {
    private record NewAssetRequest(String id, String filename, String type, String basePath, String namespace) {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            switch (path) {
                case "/rebuild" -> {
                    PolymerResourcePackMod.generateAndCall(Filament.SERVER, false, (x) -> {
                        Filament.LOGGER.info(x.getString());
                    }, result -> {
                        // todo: on finish
                    });
                    noContent(resp);
                }
                case "/reload" -> {
                    var provider = AutoHost.provider;
                    for (var player : Filament.SERVER.getPlayerList().getPlayers()) {
                        for (var x : provider.getProperties(player.connection.getPacketContext())) {
                            player.connection.send(new ClientboundResourcePackPushPacket(
                                    x.id(),
                                    x.url(),
                                    x.hash(),
                                    AutoHost.config.require || PolymerResourcePackUtils.isRequired(),
                                    Optional.ofNullable(AutoHost.message)
                            ));
                        }
                    }
                    noContent(resp);
                }
                case "/save" -> {
                    String uuidValue = req.getParameter("uuid");
                    if (uuidValue == null || uuidValue.isBlank()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing uuid");
                        return;
                    }

                    var asset = AssetStore.getAsset(UUID.fromString(uuidValue));
                    if (asset != null) {
                        asset.writeFile();
                    }
                    noContent(resp);
                }
                case "/reregister" -> {
                    String uuidValue = req.getParameter("uuid");
                    if (uuidValue == null || uuidValue.isBlank()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing uuid");
                        return;
                    }

                    var asset = AssetStore.getAsset(UUID.fromString(uuidValue));
                    if (asset != null) {
                        asset.writeFile();
                        asset.runtimeRegister();
                    }
                    noContent(resp);
                }
                case "/new" -> renderNewForm(resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid uuid");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            if (path.equals("/new")) {
                handleNewSubmit(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void renderNewForm(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");

        List<Path> paths = new ArrayList<>();
        PackRepository packRepo = Filament.SERVER.getPackRepository();
        for (Pack pack : packRepo.getSelectedPacks()) {
            Pack.ResourcesSupplier packResources = ((PackAccessor)pack).getResources();
            if (packResources instanceof PathPackResources.PathResourcesSupplier pathResourcesSupplier) {
                var path = ((PathResourcesSupplierAccessor)pathResourcesSupplier).getContent();
                paths.add(path);
            }
        }

        String html = div().withClass("p-3").with(
                h3("Create new asset").withClass("mb-3"),
                form().withClass("d-grid gap-3").attr("hx-post", "/action/new").attr("hx-target", "#editor-pane").attr("hx-swap", "innerHTML").with(
                        div().with(
                                label("Target Folder").withClass("form-label").attr("for", "new-basepath"),
                                select(
                                        paths.stream().map(p ->
                                                option(p.toString()).withValue(p.toString())
                                        ).toArray(j2html.tags.DomContent[]::new)
                                ).withId("new-basepath")
                                        .withName("basePath")
                                        .withClass("form-select")
                                        .attr("required", "true")
                        ),

                        div().with(
                                label("Namespace").withClass("form-label").attr("for", "new-namespace"),
                                input()
                                        .withType("text")
                                        .withId("new-namespace")
                                        .withName("namespace")
                                        .withClass("form-control")
                                        .withPlaceholder("mynamespace")
                                        .attr("required", "true")
                        ),

                        div().with(
                                label("Id").withClass("form-label").attr("for", "new-id"),
                                input()
                                        .withType("text")
                                        .withId("new-id")
                                        .withName("id")
                                        .withClass("form-control")
                                        .withPlaceholder("mynamespace:my_item")
                                        .attr("required", "true")
                        ),

                        div().with(
                                label("Filename").withClass("form-label").attr("for", "new-filename"),
                                input()
                                        .withType("text")
                                        .withId("new-filename")
                                        .withName("filename")
                                        .withClass("form-control")
                                        .withPlaceholder("example.json")
                                        .attr("required", "true")
                        ),
                        div().with(
                                label("Type").withClass("form-label").attr("for", "new-type"),
                                select(
                                        option("Item").withValue("item"),
                                        option("Block").withValue("block"),
                                        option("Decoration").withValue("decoration")
                                ).withId("new-type")
                                        .withName("type")
                                        .withClass("form-select")
                                        .attr("required", "true")
                        ),
                        button("Create")
                                .withType("submit")
                                .withClass("btn btn-success")
                )
        ).render();

        resp.getWriter().write(html);
    }

    private void handleNewSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = trimToNull(req.getParameter("filename"));
        String id = trimToNull(req.getParameter("id"));
        String type = trimToNull(req.getParameter("type"));
        String basePath = trimToNull(req.getParameter("basePath"));
        String namespace = trimToNull(req.getParameter("namespace"));

        if (filename == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing filename");
            return;
        }
        if (type == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing type");
            return;
        }
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Id");
            return;
        }
        if (basePath == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing target path");
            return;
        }
        if (namespace == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing namespace");
            return;
        }

        NewAssetRequest request = new NewAssetRequest(id, filename, type, basePath, namespace);
        Asset created = createNewAsset(request);
        created.writeFile();
        created.runtimeRegister();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");


        String editorPane = ReadFileServlet.renderOob(created.uuid.toString()).render();

        String uuidField = input()
                .withType("hidden")
                .withId("current-file-uuid")
                .withName("uuid")
                .withValue(created.uuid.toString())
                .attr("hx-swap-oob", "true")
                .render();

        resp.getWriter().write(editorPane);
        resp.getWriter().write(uuidField);
    }

    private Asset createNewAsset(NewAssetRequest request) {
        Type t = switch (request.type()) {
            case "item" -> ItemData.class;
            case "block" -> BlockData.class;
            case "decoration" -> DecorationData.class;
            default -> throw new IllegalArgumentException("Unknown type: " + request.type());
        };


        UUID uuid = UUID.randomUUID();
        var asset = new Asset();
        asset.uuid = uuid;

        asset.path = Path.of(request.basePath()).resolve("data").resolve(request.namespace).resolve("filament").resolve(request.type).resolve(request.filename);
        asset.path.getParent().toFile().mkdirs();

        asset.data = Json.GSON.fromJson(String.format("""
                {
                    "id": "%s"
                }
                """, request.id()), t);

        asset.type = t;
        AssetStore.registerAsset(asset);

        return asset;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isBlank() ? null : v;
    }

    private static void noContent(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}