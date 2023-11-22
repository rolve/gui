package gui.impl.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.Integer.parseInt;

public class WebGuiServer {

    public static Integer port() {
        var port = System.getProperty("gui.port");
        return port != null ? parseInt(port) : null;
    }

    private final Server server;

    WebGuiServer() {
        var port = port();
        server = new Server(port);
        var handler = new ServletContextHandler(null, "/");
        handler.addServlet(WebGuiServlet.class, "/ws/");
        handler.addServlet(IndexServlet.class, "/");
        server.setHandler(handler);
    }

    void start() throws Exception {
        server.start();
        server.dump(System.err);
        server.join();
    }

    public static class IndexServlet extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            if (!req.getRequestURI().equals("/")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/html");
            try (var in = IndexServlet.class.getResourceAsStream("/static/index.html")) {
                in.transferTo(resp.getOutputStream());
            }
        }
    }

    public static class WebGuiServlet extends WebSocketServlet {
        public void configure(WebSocketServletFactory factory) {
            factory.register(WebGuiSocket.class);
        }
    }
}
