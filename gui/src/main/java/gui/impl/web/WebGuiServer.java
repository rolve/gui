package gui.impl.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.websocket.server.ServerEndpointConfig.Builder.create;
import static java.lang.Integer.parseInt;
import static org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;

public class WebGuiServer {

    public static Integer port() {
        var port = System.getProperty("gui.port");
        return port != null ? parseInt(port) : null;
    }

    private final Server server;

    WebGuiServer() {
        server = new Server(port());
        var handler = new ServletContextHandler(null, "/");
        handler.addServlet(IndexServlet.class, "/");
        server.setHandler(handler);
        configure(handler, (context, container) -> {
            container.addEndpoint(create(WebGuiEndpoint.class, "/ws").build());
        });
    }

    void start() throws Exception {
        server.start();
        server.join();
    }

    public static class IndexServlet extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            if (!req.getRequestURI().equals("/")) {
                resp.setStatus(SC_NOT_FOUND);
                return;
            }
            resp.setStatus(SC_OK);
            resp.setContentType("text/html");
            try (var in = IndexServlet.class.getResourceAsStream("/static/index.html")) {
                in.transferTo(resp.getOutputStream());
            }
        }
    }
}
