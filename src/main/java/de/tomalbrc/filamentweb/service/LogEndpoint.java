package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filament.Filament;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/log")
public class LogEndpoint {
    private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        Filament.LOGGER.info("New web-editor connection!");
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        Filament.LOGGER.info("Closed web-editor connection!");
    }

    public static void broadcast(String message) {
        for (Session s : SESSIONS) {
            s.getAsyncRemote().sendText(message);
        }
    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            HttpSession httpSession = (HttpSession) request.getHttpSession();

            if (httpSession == null) {
                Filament.LOGGER.warn("Unauthorized: No session found");
                throw new RuntimeException("Unauthorized: No session found");
            }

            Object sessionAttr = httpSession.getAttribute(AuthFilter.SESSION_KEY);
            if (!(sessionAttr instanceof AuthFilter.EditorSession session)) {
                Filament.LOGGER.warn("Unauthorized: Not logged in");
                throw new RuntimeException("Unauthorized: Not logged in");
            }

            boolean authorized;
            if ("Server".equals(session.username())) {
                authorized = true;
            } else if (session.player() != null) {
                var player = Filament.SERVER.getPlayerList().getPlayer(session.player());
                authorized = player != null && Permissions.check(player, "filament.editor", PermissionLevel.ADMINS);
            } else {
                authorized = true;
            }

            if (!authorized) {
                throw new RuntimeException("Unauthorized: Forbidden");
            }

        }
    }
}