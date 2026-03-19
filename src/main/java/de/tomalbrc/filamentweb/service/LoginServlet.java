package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filamentweb.EditorServer;
import de.tomalbrc.filamentweb.FilamentEditorConfig;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.HtmlTag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.UUID;

import static j2html.TagCreator.*;

public class LoginServlet extends HttpServlet {
    private static String base64;

    private final AuthFilter auth;

    public LoginServlet(AuthFilter auth) {
        this.auth = auth;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean passwordLogin = FilamentEditorConfig.getInstance().passwordLogin;

        UUID uuid = parseId(req);
        if (!passwordLogin && (uuid == null || !isValidLoginTarget(uuid))) {
            writeHtml(resp, errorPage(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request",
                    "The link is invalid or expired."
            ).render());
            return;
        }

        var player = uuid == null ? null : Filament.SERVER.getPlayerList().getPlayer(uuid);
        String playerName = player == null ? "Server" : player.getScoreboardName();
        writeHtml(resp, loginPage(null, uuid, playerName, passwordLogin).render());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("user");
        String password = req.getParameter("pass");
        String id = req.getParameter("id");
        boolean passwordLogin = FilamentEditorConfig.getInstance().passwordLogin;

        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            if (!passwordLogin) {
                writeHtml(resp, errorPage(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid request",
                        "The link is invalid or expired."
                ).render());
                return;
            }
        }

        if (!passwordLogin && !isValidLoginTarget(uuid)) {
            writeHtml(resp, errorPage(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request",
                    "The link is invalid or expired."
            ).render());
            return;
        }

        var player = uuid == null ? null : Filament.SERVER.getPlayerList().getPlayer(uuid);
        String playerName = player == null ? "Server" : player.getScoreboardName();
        String expectedKey = passwordLogin ? null : expectedKey(uuid);

        boolean authOk;
        if (passwordLogin) {
            authOk = auth.matches(username, password);
        } else {
            authOk = expectedKey != null && expectedKey.equals(password);
        }

        if (authOk) {
            if (uuid != null) {
                AuthFilter.SERVER_REQUEST.remove(uuid);
                AuthFilter.REQUESTS.remove(uuid);
            }

            String effectiveUsername = passwordLogin ? username : playerName;

            req.getSession(true).setAttribute(
                    AuthFilter.SESSION_KEY,
                    new AuthFilter.EditorSession(effectiveUsername, UUID.randomUUID(), req.getRemoteAddr(), uuid)
            );

            if (req.getHeader("HX-Request") != null) {
                resp.setHeader("HX-Redirect", "/");
            } else {
                resp.sendRedirect("/");
            }
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeHtml(resp, loginPage("Wrong credentials", uuid, playerName, passwordLogin).render());
    }

    private UUID parseId(HttpServletRequest req) {
        try {
            return UUID.fromString(req.getParameter("id"));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    private boolean isValidLoginTarget(UUID uuid) {
        var player = Filament.SERVER.getPlayerList().getPlayer(uuid);
        return (player != null && Permissions.check(player, "filament.editor")) || (AuthFilter.REQUESTS.containsKey(uuid) || AuthFilter.SERVER_REQUEST.containsKey(uuid));
    }

    private String expectedKey(UUID uuid) {
        String key = AuthFilter.REQUESTS.get(uuid);
        if (key == null) {
            key = AuthFilter.SERVER_REQUEST.get(uuid);
        }
        return key;
    }

    private ContainerTag<?> errorPage(int code, String titleText, String message) {
        byte[] img = EditorServer.resourcePackBuilder().getDataOrSource(
                AssetPaths.texture(Identifier.withDefaultNamespace("block/dirt")) + ".png"
        );
        var base64 = java.util.Base64.getEncoder().encodeToString(img);

        return html(
                head(
                        title("Error " + code),
                        link().withRel("stylesheet")
                                .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"),
                        style(
                                "body {" +
                                        "  background-image: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), " +
                                        "                    url('data:image/png;base64," + base64 + "');" +
                                        "  background-repeat: repeat;" +
                                        "  background-size: 64px 64px;" +
                                        "  image-rendering: pixelated;" +
                                        "  height: 100vh; margin: 0;" +
                                        "}" +
                                        ".overlay {" +
                                        "  height: 100%; display:flex; align-items:center; justify-content:center;" +
                                        "  backdrop-filter: blur(5px);" +
                                        "}" +
                                        ".card { border-radius: 6px; }"
                        )
                ),
                body().with(
                        div().withClass("overlay").with(
                                div().withClass("card bg-dark text-white shadow-lg p-4 text-center")
                                        .withStyle("max-width: 420px;").with(
                                                h1(String.valueOf(code)).withClass("fw-bold mb-3"),
                                                h4(titleText).withClass("mb-3"),
                                                p(message).withClass("text-secondary mb-4"),

                                                a("Back to login")
                                                        .withHref("/login")
                                                        .withClass("btn btn-primary fw-bold")
                                        )
                        )
                )
        );
    }

    private void writeHtml(HttpServletResponse resp, String html) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write(html);
    }

    private ContainerTag<HtmlTag> loginPage(String error, UUID id, String playerName, boolean passwordLogin) {
        if (base64 == null) {
            byte[] img = EditorServer.resourcePackBuilder().getDataOrSource(
                    AssetPaths.texture(Identifier.withDefaultNamespace("block/dirt")) + ".png"
            );
            base64 = java.util.Base64.getEncoder().encodeToString(img);
        }

        ContainerTag<?> credentialBlock;
        if (passwordLogin) {
            credentialBlock = div(
                    div().withClass("form-floating mb-3").with(
                            input()
                                    .withType("text")
                                    .withName("user")
                                    .withClass("form-control bg-dark text-white border-secondary")
                                    .withId("uInput")
                                    .attr("autocomplete", "username")
                                    .isRequired(),
                            label("Username").withFor("uInput").withClass("text-secondary")
                    ),
                    div().withClass("form-floating mb-4").with(
                            input()
                                    .withType("password")
                                    .withName("pass")
                                    .withClass("form-control bg-dark text-white border-secondary")
                                    .withId("pInput")
                                    .withPlaceholder("XXXXX")
                                    .isRequired(),
                            label("Password").withFor("pInput").withClass("text-secondary")
                    )
            );
        } else {
            credentialBlock = div(
                    div().withClass("form-floating mb-3").with(
                            input()
                                    .withType("text")
                                    .withName("user")
                                    .withClass("form-control bg-dark text-white border-secondary")
                                    .withId("uInput")
                                    .withPlaceholder("Username")
                                    .attr("autocomplete", "username")
                                    .attr("readonly", "readonly")
                                    .withValue(playerName != null ? playerName : ""),
                            label("Username").withFor("uInput").withClass("text-secondary")
                    ),
                    div().withClass("form-floating mb-4").with(
                            input()
                                    .withType("password")
                                    .withName("pass")
                                    .withClass("form-control bg-dark text-white border-secondary")
                                    .withId("pInput")
                                    .withPlaceholder("Key")
                                    .attr("autocomplete", "off")
                                    .isRequired(),
                            label("Key").withFor("pInput").withClass("text-secondary")
                    )
            );
        }

        return html(
                head(
                        title("FAE - Login"),
                        link().withRel("stylesheet").withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"),
                        script().withSrc("https://unpkg.com/htmx.org@1.9.12"),
                        script().withSrc("https://unpkg.com/hyperscript.org@0.9.14"),
                        style(
                                "body {" +
                                        "  background-image: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), " +
                                        "                    url('data:image/png;base64," + base64 + "');" +
                                        "  background-repeat: repeat;" +
                                        "  background-size: 64px 64px;" +
                                        "  image-rendering: pixelated;" +
                                        "  height: 100vh; margin: 0;" +
                                        "}" +
                                        ".login-overlay {" +
                                        "  height: 100%; width: 100%;" +
                                        "  display: flex; align-items: center; justify-content: center;" +
                                        "  backdrop-filter: blur(4px);" +
                                        "}" +
                                        ".form-control { border-radius: 2px; }"
                        )
                ),
                body().with(
                        div().withClass("login-overlay").with(
                                div().withClass("card bg-dark text-white shadow-lg p-4").withId("login-card").withStyle("width: 380px;").with(
                                        div().withClass("text-center mb-4").with(
                                                h3("Filament Editor").withClass("fw-bold")
                                        ),

                                        div().withClass("text-secondary small text-center mb-3")
                                                .withText((playerName != null ? playerName : "unknown")),

                                        (error != null
                                                ? div(error).withClass("alert alert-danger py-2 text-center border-0 small mb-3")
                                                : div()),

                                        form().attr("hx-post", "/login")
                                                .attr("hx-target", "#login-card")
                                                .attr("hx-swap", "outerHTML")
                                                .attr("_", "on submit set #login-btn.disabled to true put 'Working...' into #login-btn")
                                                .with(
                                                        input().withType("hidden").withName("id").withValue(id == null ? "" : id.toString()),

                                                        credentialBlock,

                                                        button("Login")
                                                                .withId("login-btn")
                                                                .withType("submit")
                                                                .withClass("btn btn-primary w-100 py-2 fw-bold")
                                                )
                                )
                        )
                )
        );
    }
}