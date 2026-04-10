package de.tomalbrc.filamentweb.service;

import de.tomalbrc.filamentweb.SchemaFormBuilder;
import j2html.tags.ContainerTag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
}