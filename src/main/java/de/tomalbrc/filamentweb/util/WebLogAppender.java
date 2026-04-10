package de.tomalbrc.filamentweb.util;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filamentweb.service.LogEndpoint;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;

public class WebLogAppender extends AbstractAppender {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    protected WebLogAppender() {
        super("WebLogAppender", null, null, true, null);
    }

    @Override
    public void append(LogEvent event) {
        String msg = event.getMessage().getFormattedMessage() + (event.getThrown() == null ? "" : ": " + event.getThrown().getMessage());
        String level = event.getLevel().name().toLowerCase();
        String timestamp = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeMillis()));

        String badgeClass = switch (level) {
            case "error" -> "bg-danger text-white";
            case "warn" -> "bg-warning text-dark";
            case "info" -> "bg-info text-dark";
            case "debug" -> "bg-secondary text-white";
            case "trace" -> "bg-dark text-light";
            default -> "bg-light text-dark";
        };

        String colorClass = switch (level) {
            case "error" -> "text-danger";
            case "warn" -> "text-warning";
            case "info" -> "text-info";
            case "debug" -> "text-secondary";
            default -> "text-light";
        };

        LogEndpoint.broadcast(div()
                .withId("console-log")
                .attr("hx-swap-oob", "beforeend")
                .with(
                        div().withClass("d-flex align-items-start gap-2 m-0 p-0")
                                .withStyle("line-height:1.2;font-size:0.75rem;")
                                .with(
                                        span(level.toUpperCase())
                                                .withClass("badge rounded-pill " + badgeClass)
                                                .withStyle("""
                                                        width:2.5rem;
                                                        text-align:center;
                                                        font-family:monospace;
                                                        flex-shrink:0;
                                                        """),
                                        span(timestamp)
                                                .withClass("text-muted")
                                                .withStyle("font-family:monospace; flex-shrink:0;"),
                                        span(msg).withClass(colorClass)
                                )
                ).render());
    }

    public static void install() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        WebLogAppender appender = new WebLogAppender();
        appender.start();

        LoggerConfig loggerConfig = new LoggerConfig(Filament.LOGGER.getName(), Level.ALL, true);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(Filament.LOGGER.getName(), loggerConfig);

        ctx.updateLoggers();
    }
}