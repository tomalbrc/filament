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

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;

public class WebLogAppender extends AbstractAppender {

    protected WebLogAppender() {
        super("WebLogAppender", null, null, true, null);
    }

    @Override
    public void append(LogEvent event) {
        String msg = event.getMessage().getFormattedMessage();
        String level = event.getLevel().name().toLowerCase();

        String badgeClass = switch (level) {
            case "error" -> "bg-danger text-white";
            case "warn"  -> "bg-warning text-dark";
            case "info"  -> "bg-info text-dark";
            case "debug" -> "bg-secondary text-white";
            case "trace" -> "bg-dark text-light";
            default      -> "bg-light text-dark";
        };

        String colorClass = switch (level) {
            case "error" -> "text-danger";
            case "warn"  -> "text-warning";
            case "info"  -> "text-info";
            case "debug" -> "text-secondary";
            default      -> "text-light";
        };

        String html = div()
                .withId("console-log")
                .attr("hx-swap-oob", "beforeend")
                .with(
                        div().withClass("d-flex align-items-start gap-2 m-0 p-0")
                                .withStyle("line-height:1.2;font-size:0.85rem;")
                                .with(
                                        span(level.toUpperCase())
                                                .withClass("badge rounded-pill " + badgeClass)
                                                .withStyle("""
                                                    width:3.5rem;
                                                    text-align:center;
                                                    font-family:monospace;
                                                    flex-shrink:0;
                                                    """),

                                        span(msg).withClass(colorClass)
                                )
                ).render();

        LogEndpoint.broadcast(html);
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