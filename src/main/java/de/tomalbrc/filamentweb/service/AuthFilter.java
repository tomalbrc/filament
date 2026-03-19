package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filamentweb.FilamentEditorConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthFilter implements Filter {
    public static final String SESSION_KEY = "SESSION_KEY";
    public static final String PLAYER_KEY = "PLAYER_KEY";

    public static Map<UUID, String> REQUESTS = new ConcurrentHashMap<>();
    public static Map<UUID, String> SERVER_REQUEST = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        if (path.equals("/login") || path.equals("/logout")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            loginRedirect(request, response);
            return;
        }

        boolean authenticated = false;
        if (httpSession.getAttribute(SESSION_KEY) instanceof EditorSession session) {
            var sameRemote = session.address().equals(request.getRemoteAddr());

            if (sameRemote) {
                if (session.username.equals("Server")) {
                    authenticated = true;
                } else {
                    if (session.player != null) {
                        var player = Filament.SERVER.getPlayerList().getPlayer(session.player);
                        authenticated = player != null && Permissions.check(player, "filament.editor");
                    } else {
                        authenticated = true;
                    }
                }
            }
        }

        if (!authenticated) {
            loginRedirect(request, response);
            return;
        }

        chain.doFilter(req, res);
    }

    private static void loginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getHeader("HX-Request") != null) {
            response.setHeader("HX-Redirect", "/login");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.sendRedirect("/login");
        }
    }

    public boolean matches(String username, String password) {
        var c = FilamentEditorConfig.getInstance();
        return c.passwordLogin && Objects.equals(c.defaultUser, username) && Objects.equals(c.defaultPassword, password);
    }

    public record EditorSession(String username, UUID sessionId, String address, @Nullable UUID player) {
    }
}

