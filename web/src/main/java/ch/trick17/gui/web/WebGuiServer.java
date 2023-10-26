package ch.trick17.gui.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;

public class WebGuiServer {

    public static void main(String[] args) throws Exception {
        new WebGuiServer().start();
    }

    private final Server server;

    public WebGuiServer() {
        server = new Server(8080);

        var webSocketHandler = new ServletContextHandler();
        webSocketHandler.setContextPath("/ws");
        webSocketHandler.addServlet(WebGuiServlet.class, "/");

        var resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(newClassPathResource("/static"));

        server.setHandler(new HandlerList(webSocketHandler, resourceHandler));
    }

    public void start() throws Exception {
        server.start();
        server.dump(System.err);
        server.join();
    }

    public static class WebGuiServlet extends WebSocketServlet {
        public void configure(WebSocketServletFactory factory) {
            factory.register(WebGuiSocket.class);
        }
    }
}
