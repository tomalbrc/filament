package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filamentweb.asset.Asset;
import de.tomalbrc.filamentweb.asset.AssetStore;
import de.tomalbrc.filamentweb.util.WebPaths;
import j2html.tags.DomContent;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

import static j2html.TagCreator.*;

public class FileListServlet extends HttpServlet {

    private static class Node {
        final String name;
        final Map<String, Node> children = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final List<Asset> files = new ArrayList<>();

        Node(String name) {
            this.name = name;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        String search = req.getParameter("search");
        if (search != null && !search.isBlank()) {
            search = search.toLowerCase(Locale.ROOT);
        } else {
            search = null;
        }

        List<Asset> treeAssets = new ArrayList<>();
        List<Asset> readOnlyAssets = new ArrayList<>();
        List<Path> folderPaths = new ArrayList<>();

        for (Asset asset : AssetStore.getAssetsByUuid().values()) {
            try {
                String idText = asset.data.id().toString().toLowerCase(Locale.ROOT);
                String pathText = asset.path == null ? "" : asset.path.toString().toLowerCase(Locale.ROOT);

                if (search != null && !idText.contains(search) && !pathText.contains(search)) {
                    continue;
                }

                if (asset.path == null) {
                    readOnlyAssets.add(asset);
                } else {
                    Path normalized = asset.path.normalize();
                    treeAssets.add(asset);
                    Path parent = normalized.getParent();
                    if (parent != null) {
                        folderPaths.add(parent);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        Path sharedPrefix = commonPrefix(folderPaths);

        Node root = new Node("");
        for (Asset asset : treeAssets) {
            Path normalized = asset.path.normalize();
            Path parent = normalized.getParent();
            Path relParent = stripPrefix(parent, sharedPrefix);
            insert(root, relParent, asset);
        }

        DomContent treeContainer = renderTreeRows(root, 0);

        List<DomContent> readOnlyDom = new ArrayList<>();
        for (Asset asset : readOnlyAssets) {
            readOnlyDom.add(renderReadOnlyRow(asset));
        }

        PrintWriter out = res.getWriter();

        out.println(
                div().withClass("w-100 overflow-hidden")
                        .with(
                                style(".folder-toggle[aria-expanded=\"false\"] .chevron { transform: rotate(-90deg); }\n" +
                                        ".chevron { display: inline-block; transition: transform 0.2s ease; }"),
                                treeContainer,
                                readOnlyDom.isEmpty() ? text("") : div().withClass("list-group list-group-flush w-100 border-top mt-2").with(readOnlyDom)
                        ).render()
        );
    }

    private static void insert(Node root, Path parentPath, Asset asset) {
        Node cur = root;

        if (parentPath != null) {
            parentPath = parentPath.normalize();
            for (Path part : parentPath) {
                String segment = part.toString();
                cur = cur.children.computeIfAbsent(segment, Node::new);
            }
        }

        cur.files.add(asset);
    }

    private static DomContent renderTreeRows(Node node, int depth) {
        List<DomContent> children = new ArrayList<>();

        for (Node child : node.children.values()) {
            children.add(renderTreeRows(child, depth + 1));
        }

        node.files.sort(Comparator.comparing(a -> a.data.id().toString(), String.CASE_INSENSITIVE_ORDER));
        for (Asset asset : node.files) {
            children.add(renderFileRow(asset, depth));
        }

        if (depth == 0) {
            return div().withClass("list-group list-group-flush w-100 p-0").with(children);
        } else {
            String folderId = "f-" + UUID.randomUUID();

            return div().with(
                    a().withClass("list-group-item list-group-item-action list-group-item-dark text-truncate border-0 py-2 d-flex align-items-center rounded-0 folder-toggle")
                            .attr("data-bs-toggle", "collapse")
                            .attr("href", "#" + folderId)
                            .attr("role", "button")
                            .attr("aria-expanded", "true")
                            .attr("style", "padding-left:" + (depth * 5) + "px; cursor: pointer;")
                            .with(
                                    span("▼").withClass("chevron me-2").attr("style", "font-size: 0.6rem;"),
                                    span("📁 " + node.name).withClass("fw-semibold text-truncate")
                            ),
                    div().withId(folderId).withClass("collapse show").with(children)
            );
        }
    }

    private static DomContent renderFileRow(Asset asset, int depth) {
        String name = displayName(asset);
        String id = asset.data.id().toString();
        String pathText = asset.path == null ? "" : asset.path.toString();

        return a()
                .withClass("list-group-item list-group-item-action border-0 py-1 pe-2 rounded-0")
                .attr("style", "cursor:pointer; padding-left:" + (depth * 5 + 12) + "px;")
                .attr("hx-get", WebPaths.file(asset.uuid.toString()))
                .attr("hx-target", "#editor-pane")
                .attr("hx-swap", "innerHTML")
                .attr("title", "Open " + id)
                .with(
                        div(asset.icon() + name).withClass("fw-medium text-truncate"),
                        div(id).withClass("text-muted text-truncate").attr("style", "font-size: 0.7rem;"),
                        pathText.isEmpty() ? text("") : div(pathText).withClass("text-muted text-truncate").attr("style", "font-size: 0.4rem;")
                );
    }

    private static DomContent renderReadOnlyRow(Asset asset) {
        String id = asset.data.id().toString();

        return a()
                .withClass("list-group-item list-group-item-action border-0 py-1 px-2 rounded-0")
                .attr("style", "cursor:pointer;")
                .attr("hx-get", WebPaths.file(asset.uuid.toString()))
                .attr("hx-target", "#editor-pane")
                .attr("hx-swap", "innerHTML")
                .attr("title", "Open " + id)
                .with(
                        div().withClass("d-flex align-items-center text-truncate").with(
                                span("RO").withClass("badge rounded-pill text-bg-info me-2").attr("style", "font-size: 0.6rem;"),
                                span("📄 " + id).withClass("fw-medium text-truncate")
                        ),
                        div(id).withClass("text-muted text-truncate").attr("style", "font-size: 0.7rem;")
                );
    }

    private static String displayName(Asset asset) {
        if (asset.path == null) {
            return asset.data.id().toString();
        }
        Path fileName = asset.path.getFileName();
        return fileName == null ? asset.path.toString() : fileName.toString();
    }

    private static Path stripPrefix(Path path, Path prefix) {
        if (path == null) return null;
        Path normalized = path.normalize();
        if (prefix == null) return normalized;
        if (!normalized.startsWith(prefix)) return normalized;

        Path rel = prefix.relativize(normalized);
        return rel.getNameCount() == 0 ? normalized : rel;
    }

    private static Path commonPrefix(List<Path> paths) {
        // todo: hmm
        if (true) {
            return Path.of("world", "datapacks");
        }

        if (paths == null || paths.size() < 2) return null;

        List<Path> normalized = new ArrayList<>();
        for (Path p : paths) {
            if (p != null && p.getNameCount() > 0) {
                normalized.add(p.normalize());
            }
        }
        if (normalized.size() < 2) return null;

        Path first = normalized.getFirst();
        int max = first.getNameCount();
        int prefixLen = 0;

        outer:
        for (int i = 0; i < max; i++) {
            String segment = first.getName(i).toString();
            for (int j = 1; j < normalized.size(); j++) {
                Path other = normalized.get(j);
                if (other.getNameCount() <= i || !other.getName(i).toString().equals(segment)) {
                    break outer;
                }
            }
            prefixLen++;
        }

        if (prefixLen == 0) return null;

        Path prefix = first.getName(0);
        for (int i = 1; i < prefixLen; i++) {
            prefix = prefix.resolve(first.getName(i));
        }
        return prefix;
    }
}