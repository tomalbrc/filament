package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filamentweb.util.WebPaths;
import j2html.tags.Tag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static j2html.TagCreator.*;

public class AssetEditorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                                script().withSrc("https://unpkg.com/htmx.org@1.9.12/dist/ext/ws.js"),
                                script().withSrc("https://unpkg.com/hyperscript.org@0.9.14"),
                                script().withType("module").withSrc("https://ajax.googleapis.com/ajax/libs/model-viewer/4.2.0/model-viewer.min.js"),

                                link().withRel("stylesheet").withHref("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/tokyo-night-dark.min.css"),
                                script().withSrc("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js"),
                                script().withSrc("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/languages/json.min.js"),

                                link().withRel("stylesheet").withHref("https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.css"),
                                script().withSrc("https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.js"),

                                link().withRel("stylesheet").withHref("https://cdnjs.cloudflare.com/ajax/libs/hint.css/3.0.0/hint.min.css"),

                                // todo: sep. css
                                style("""
                                        [data-bs-theme="dark"] {
                                            --bs-body-bg: #1a1b26;
                                            --bs-body-color: #a9b1d6;
                                            --bs-tertiary-bg: #24283b;
                                            --bs-secondary-bg: #292e42;
                                            --bs-border-color: #414868;
                                            --bs-primary: #7aa2f7;
                                            --bs-primary-rgb: 122, 162, 247;
                                            --bs-success: #9ece6a;
                                            --bs-info: #7dcfff;
                                            --bs-warning: #e0af68;
                                            --bs-danger: #f7768e;
                                            --bs-link-color: #7aa2f7;
                                            --bs-link-hover-color: #89b4fa;
                                        }
                                        
                                        .form-control, .form-select { background-color: #1a1b26; border-color: #414868; color: #c0caf5; }
                                        .form-control:focus, .form-select:focus { border-color: #7aa2f7; box-shadow: 0 0 0 0.25rem rgba(122, 162, 247, 0.25); background-color: #1a1b26; color: #c0caf5; }
                                        ::-webkit-scrollbar { width: 8px; height: 8px; }
                                        ::-webkit-scrollbar-track { background: #1a1b26; }
                                        ::-webkit-scrollbar-thumb { background: #414868; border-radius: 4px; }
                                        ::-webkit-scrollbar-thumb:hover { background: #7aa2f7; }
                                        
                                        .nested-wrap { border-left: 2px solid #292e42; transition: border-color 0.2s; margin-left: 2px; }
                                        .nested-wrap:hover { border-left-color: #7aa2f7; }
                                        .array-section { border-left: 3px solid #e0af68 !important; }
                                        .composed-container { border-left: 3px solid #7dcfff !important; }
                                        .object-section, .composed-section { border-left: 3px solid #bb9af7 !important; }
                                        
                                        .btn-remove { color: #f7768e !important; opacity: 0.7; transition: opacity 0.2s; }
                                        .btn-remove:hover { opacity: 1; text-shadow: 0 0 8px rgba(247, 118, 142, 0.4); border: 1px solid #f7768e; }
                                        .navbar-brand { font-weight: 600; }
                                        """) // nested-wrap is unused rn
                        ),
                        body().withClass("d-flex flex-column vh-100").attr("data-bs-theme", "dark").with(
                                input()
                                        .withType("hidden")
                                        .withId("current-file-uuid")
                                        .withValue("")
                                        .attr("hx-swap-oob", "true"),

                                nav().withClass("navbar navbar-expand bg-body-tertiary border-bottom sticky-top shadow-sm").with(
                                        div().withClass("container-fluid d-flex justify-content-between align-items-center").with(
                                                span("Filament Editor").withClass("navbar-brand mb-0 h1"),

                                                div().withClass("d-flex align-items-center gap-2").with(

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

                                                        div().withClass("btn-group px-5").attr("role", "group").with(
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
                                renderConsole(),
                                div().withClass("container-fluid p-0 overflow-hidden").with(
                                        div().withClass("container-fluid vh-100 d-flex flex-column").with(
                                                div().withClass("row flex-grow-1").with(
                                                        div().withId("sidebar")
                                                                .withClass("col-2 border-end p-0 vh-100 d-flex flex-column")
                                                                .with(
                                                                        div().withClass("bg-body-secondary p-1 border-bottom shadow-sm").withStyle("height: 55px;").with(
                                                                                input().withType("text")
                                                                                        .withId("file-search")
                                                                                        .withClass("form-control")
                                                                                        .withPlaceholder("Search...")
                                                                                        .withName("search")
                                                                                        .attr("hx-get", WebPaths.files())
                                                                                        .attr("hx-target", "#files-list")
                                                                                        .attr("hx-trigger", "keyup changed delay:100ms")
                                                                        ),

                                                                        div().withClass("overflow-auto flex-grow-1").with(
                                                                                table().withClass("table table-hover mb-0").with(
                                                                                        tbody().withId("files-list")
                                                                                                .attr("hx-get", WebPaths.files())
                                                                                                .attr("hx-trigger", "load")
                                                                                )
                                                                        )
                                                                ),

                                                        div().withId("main-panel").withClass("col-10 g-0 h-100 overflow-auto").with(
                                                                div().withId("editor-pane")
                                                        )
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

    Tag<?> renderConsole() {
        return div().withId("console-wrapper")
                .attr("hx-ext", "ws")
                .attr("ws-connect", "/ws/log")
                .withClass("position-fixed bottom-0 end-0 m-3 shadow-lg border rounded bg-body-secondary")
                .withStyle("z-index: 9999; width: 50%;")
                .with(
                        div().withClass("d-flex justify-content-between align-items-center p-2 border-bottom bg-tertiary-bg text-white rounded-top")
                                .with(
                                        div("Console").withClass("small fw-bold").with(
                                                span().withId("status-dot")
                                                        .withClass("d-inline-block rounded-circle ms-2 shadow-sm")
                                                        .withStyle("width: 10px; height: 10px; background-color: #e0af68;")
                                                        .attr("_", """
                                                                      on htmx:wsOpen from #console-wrapper set my.style.backgroundColor to '#9ece6a'
                                                                      on htmx:wsClose from #console-wrapper set my.style.backgroundColor to '#f7768e'
                                                                      on htmx:wsError from #console-wrapper set my.style.backgroundColor to '#f7768e'
                                                                """)
                                        ),

                                        div().withClass("d-flex gap-1").with(
                                                button("Clear")
                                                        .withClass("btn btn-sm btn-outline-danger py-0 px-2")
                                                        .withStyle("font-size: 0.7rem;")
                                                        .attr("_", "on click put '' into #console-log"),

                                                button("—")
                                                        .withId("console-toggle-btn")
                                                        .withClass("btn btn-sm btn-outline-secondary py-0 px-2")
                                                        .attr("_", """
                                                                on click
                                                                    toggle .d-none on #console-body
                                                                    if #console-body matches .d-none
                                                                        set my.innerText to '▢'
                                                                        set #console-wrapper's style.width to '200px'
                                                                    else
                                                                        set my.innerText to '—'
                                                                        set #console-wrapper's style.width to '50%'
                                                                    end
                                                                """)
                                        )
                                ),

                        div().withId("console-body").withClass("p-0").with(
                                pre().withId("console-log")
                                        .withClass("mb-0 p-2 bg-body small")
                                        .withStyle("height: 250px; overflow-y: auto; font-family: monospace; white-space: pre-wrap; color: #a9b1d6;")
                                        .attr("_", "on htmx:wsAfterMessage from document set my.scrollTop to my.scrollHeight")
                        )
                );
    }
}