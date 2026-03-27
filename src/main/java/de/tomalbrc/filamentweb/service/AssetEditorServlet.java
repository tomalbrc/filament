package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filamentweb.util.WebPaths;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static j2html.TagCreator.*;

public class AssetEditorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var player = req.getSession().getAttribute("player");

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String renderedHtml = renderEditorPage();
        resp.getWriter().write(renderedHtml);
    }

    private String renderEditorPage() {
        return document(
                html().withLang("en-us").with(
                        head(
                                meta().withCharset("utf-8"),
                                meta().withName("viewport").withContent("width=device-width,initial-scale=1"),
                                title("Filament Editor"),

                                link().withRel("stylesheet").withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"),
                                script().withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"),
                                script().withSrc("https://unpkg.com/htmx.org@1.9.12"),
                                script().withSrc("https://unpkg.com/hyperscript.org@0.9.14"),
                                script().withType("module").withSrc("https://ajax.googleapis.com/ajax/libs/model-viewer/4.2.0/model-viewer.min.js"),

                                link().withRel("stylesheet").withHref("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/atom-one-dark.min.css"),
                                script().withSrc("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js"),
                                script().withSrc("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/languages/json.min.js"),

                                link().withRel("stylesheet").withHref("https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.css"),
                                script().withSrc("https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.js"),

                                link().withRel("stylesheet").withHref("https://cdnjs.cloudflare.com/ajax/libs/hint.css/3.0.0/hint.min.css"),
                                style("""
                                        .editor-container { overflow: scroll; }
                                        }""")
                        ),
                        body().withClass("d-flex flex-column vh-100").attr("data-bs-theme", "dark").with(
                                input()
                                        .withType("hidden")
                                        .withId("current-file-uuid")
                                        .withValue("")
                                        .attr("hx-swap-oob", "true"),

                                nav().withClass("navbar bg-body-tertiary border-bottom").with(
                                        div().withClass("container-fluid d-flex justify-content-between align-items-center").with(
                                                span("Filament Asset Editor").withClass("navbar-brand mb-0 h1"),

                                                div().withClass("d-flex gap-3 align-items-center").with(

                                                        div().withClass("d-flex gap-2").with(
                                                                button("New")
                                                                        .withId("new-btn")
                                                                        .withClass("btn btn-sm btn-outline-warning hint--bottom")
                                                                        .attr("hx-get", "/action/new")
                                                                        .attr("hx-target", "#editor-pane")
                                                                        .attr("hx-swap", "innerHTML")
                                                                        .attr("aria-label", "Create a new file"),

                                                                a("Open Docs")
                                                                        .withClass("btn btn-sm btn-outline-secondary hint--bottom")
                                                                        .withHref("/docs/")
                                                                        .attr("target", "_blank")
                                                                        .attr("rel", "noopener noreferrer")
                                                                        .attr("aria-label", "Open the offline docs in a new tab")

                                                                ),

                                                        div().withClass("btn-group").attr("role", "group").attr("aria-label", "File Actions").with(
                                                                button("Save")
                                                                        .withId("save-btn")
                                                                        .withClass("btn btn-sm btn-outline-primary hint--bottom")
                                                                        .attr("hx-get", "/action/save")
                                                                        .attr("hx-include", "#current-file-uuid")
                                                                        .attr("hx-swap", "none")
                                                                        .attr("aria-label", "Write to file"),

                                                                button("Reload behaviours")
                                                                        .withId("reregister-btn")
                                                                        .withClass("btn btn-sm btn-outline-primary hint--bottom")
                                                                        .attr("hx-get", "/action/reregister")
                                                                        .attr("hx-include", "#current-file-uuid")
                                                                        .attr("hx-swap", "none")
                                                                        .attr("aria-label", "Reloads behaviours. Everything else requires a server restart!")
                                                        ),

                                                        div().withClass("btn-group").attr("role", "group").attr("aria-label", "Resource Pack Actions").with(
                                                                button("Rebuild RP")
                                                                        .withClass("btn btn-sm btn-outline-info hint--bottom")
                                                                        .attr("hx-get", "/action/rebuild")
                                                                        .attr("hx-swap", "none")
                                                                        .attr("aria-label", "Rebuild the resourcepack"),

                                                                button("Reload RP")
                                                                        .withClass("btn btn-sm btn-outline-info hint--bottom")
                                                                        .attr("hx-get", "/action/reload")
                                                                        .attr("hx-swap", "none")
                                                                        .attr("aria-label", "Reload the resourcepack for all players")
                                                                ),

                                                        button("Logout")
                                                                .withClass("btn btn-sm btn-outline-danger")
                                                                .attr("hx-get", "/logout")
                                                                .attr("_", "on click call confirm('Are you sure you want to log out?') if it is false halt")
                                                )
                                        )
                                ),
                                div().withClass("container-fluid flex-grow-1 overflow-hidden").with(
                                        div().withClass("row h-100").with(

                                                div().withId("sidebar").withClass("col-2 border-end overflow-auto p-0 h-100").with(
                                                        div().withClass("sticky-top bg-body p-2 border-bottom").with(
                                                                input().withType("text")
                                                                        .withId("file-search")
                                                                        .withName("search")
                                                                        .withClass("form-control")
                                                                        .withPlaceholder("Search...")
                                                                        .attr("hx-get", WebPaths.files())
                                                                        .attr("hx-target", "#files-list")
                                                                        .attr("hx-trigger", "keyup changed delay:100ms")
                                                        )
                                                ).with(
                                                        div().withClass("p-0").with(
                                                                table().withClass("table table-hover").with(
                                                                        tbody().withId("files-list")
                                                                                .attr("hx-get", WebPaths.files())
                                                                                .attr("hx-trigger", "load")
                                                                )
                                                        )
                                                ),

                                                div().withId("main-panel").withClass("col-10 px-2 overflow-auto h-100").with(
                                                        div().withId("editor-pane")
                                                )
                                        )
                                ),
                                script("""
                                        hljs.addPlugin(
                                          new CopyButtonPlugin({autohide: false})
                                        );
                                        document.body.addEventListener('htmx:afterProcessNode', function(evt) {
                                            const codeBlocks = evt.detail.elt.querySelectorAll('pre code');
                                            codeBlocks.forEach((block) => {
                                                hljs.highlightAll();
                                            });
                                            document.querySelectorAll(".hljs-copy-button").forEach(btn => btn.type = "button");
                                        });
                                        """)
                        )
                )
        );
    }
}