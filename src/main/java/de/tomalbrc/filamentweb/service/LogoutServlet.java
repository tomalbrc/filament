package de.tomalbrc.filamentweb.service;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        if (req.getHeader("HX-Request") != null) {
            resp.setHeader("HX-Redirect", "/login");
        } else {
            resp.sendRedirect("/login");
        }
    }
}
