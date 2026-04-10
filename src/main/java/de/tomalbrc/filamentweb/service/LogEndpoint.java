package de.tomalbrc.filamentweb.service;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/log")
public class LogEndpoint {
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
    }

    public static void broadcast(String message) {
        for (Session s : SESSIONS) {
            s.getAsyncRemote().sendText(message);
        }
    }
}