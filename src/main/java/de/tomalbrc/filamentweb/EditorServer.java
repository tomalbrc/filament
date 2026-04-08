package de.tomalbrc.filamentweb;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filamentweb.asset.AssetStore;
import de.tomalbrc.filamentweb.service.*;
import de.tomalbrc.filamentweb.util.Mc2Glb;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import jakarta.servlet.DispatcherType;
import net.minecraft.resources.Identifier;
import org.eclipse.jetty.ee10.servlet.ResourceServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.function.Function;

public class EditorServer implements Runnable {
    private static boolean didStart = false;
    private static ResourcePackBuilder RP_BUILDER;

    public static Mc2Glb CONVERTER;

    private final Server server;

    public static ResourcePackBuilder resourcePackBuilder() {
        return RP_BUILDER;
    }

    public EditorServer() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(FilamentEditorConfig.getInstance().bindPort);
        connector.setHost(FilamentEditorConfig.getInstance().bindIp);
        server.addConnector(connector);

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        server.setHandler(handlers);

        addResourceHandler(handlers);
        addRestHandler(handlers);
    }

    private void addResourceHandler(ContextHandlerCollection contextHandlerCollection) throws Exception {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

        var webRoot = ResourceFactory.of(handler).newClassLoaderResource("docs");
        if (webRoot == null) {
            throw new IllegalStateException("docs not found on the classpath");
        }
        handler.setBaseResource(webRoot);

        AuthFilter auth = new AuthFilter();

        handler.addServlet(new ServletHolder(new ResourceServlet()), "/docs/*");
        handler.addServlet(new ServletHolder(new ActionServlet()), "/action/*");
        handler.addServlet(new ServletHolder(new LoginServlet(auth)), "/login");
        handler.addServlet(new ServletHolder(new LogoutServlet()), "/logout");
        handler.addServlet(new ServletHolder(new AssetEditorServlet()), "/");

        handler.addFilter(AuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        contextHandlerCollection.addHandler(handler);
    }

    private void addRestHandler(ContextHandlerCollection handlers) {
        ServletContextHandler apiHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiHandler.setContextPath("/rest");
        handlers.addHandler(apiHandler);

        apiHandler.addServlet(new ServletHolder(new FileListServlet()), "/files");
        apiHandler.addServlet(new ServletHolder(new ReadFileServlet()), "/file");
        apiHandler.addServlet(new ServletHolder(new FragmentServlet()), "/fragment");
    }

    public void start() throws Exception {
        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public static void runServer() {
        if (didStart || RP_BUILDER == null || Filament.SERVER == null) {
            return;
        }

        didStart = true;

        try {
            // todo: handle non-existing files.
            Function<Identifier, InputStream> modelProvider = identifier -> {
                var data = resourcePackBuilder().getDataOrSource(AssetPaths.model(identifier) + ".json");
                return data == null ? null : new ByteArrayInputStream(data);
            };
            Function<Identifier, InputStream> textureProvider = identifier -> {
                var data = resourcePackBuilder().getDataOrSource(AssetPaths.texture(identifier) + ".png");
                return data == null ? null : new ByteArrayInputStream(data);
            };

            CONVERTER = new Mc2Glb(modelProvider, textureProvider);
            AssetStore.DEFAULT_MODEL = CONVERTER.toGlb(Identifier.withDefaultNamespace("block/stone"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            EditorServer server = new EditorServer();
            var thread = new Thread(server, "Filament-Editor-Server");
            thread.start();
            Filament.LOGGER.info("Started Filament Editor server!");
        } catch (Exception e) {
            Filament.LOGGER.error("Failed to start editor server", e);
        }
    }

    @Override
    public void run() {
        try {
            start();
            join();
        } catch (Exception e) {
            Filament.LOGGER.error("Failed to start editor server", e);
        }
    }

    public static void init() {
        FilamentRegistrationEvents.ITEM.register((data, item) -> {
            AssetStore.registerAssetFromPath(data, ItemData.class, item);
        });
        FilamentRegistrationEvents.BLOCK.register((data, item, block) -> {
            AssetStore.registerAssetFromPath(data, BlockData.class, block);
        });
        FilamentRegistrationEvents.DECORATION.register((data, item, block) -> {
            AssetStore.registerAssetFromPath(data, DecorationData.class, item);
        });
        // TODO: Entity support!
    }

    public static void init(ResourcePackBuilder builder) {
        RP_BUILDER = builder;
        EditorServer.runServer();
    }
}