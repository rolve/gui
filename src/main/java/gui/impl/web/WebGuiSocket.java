package gui.impl.web;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.String.join;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WebGuiSocket extends WebSocketAdapter {

    private static Method mainMethod;
    private static final ThreadLocal<WebGuiSocket> current = new ThreadLocal<>();

    static void register(WebGui gui) {
        if (mainMethod == null) {
            // first call on the main thread: register main method
            var stackTrace = new Throwable().getStackTrace();
            var mainClass = stackTrace[stackTrace.length - 1].getClassName();
            try {
                mainMethod = Class.forName(mainClass).getMethod("main", String[].class);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new AssertionError(e);
            }

            // start web server
            new Thread(() -> {
                try {
                    new WebGuiServer().start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();

            // kill main thread
            currentThread().setUncaughtExceptionHandler((t, e) -> {});
            throw new ThreadDeath();
        } else {
            // other calls: register and initialize GUI
            current.get().gui = gui;
            gui.initialize(current.get());
        }
    }

    private WebGui gui;

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        new Thread(() -> {
            current.set(this);
            try {
                mainMethod.invoke(null, (Object) new String[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, "WebGui").start();
    }

    @Override
    public void onWebSocketText(String event) {
        gui.onEvent(event);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        gui.close();
    }

    void close() {
        getSession().close();
    }

    void send(Iterable<? extends CharSequence> commands) {
        try {
            getSession().getRemote().sendString(join("\n", commands));
        } catch (WebSocketException e) {
            if (!e.getMessage().contains("Session closed")) {
                throw e;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void sentImage(String name, byte[] image) {
        try {
            var nameBytes = name.getBytes(UTF_8);
            var buffer = ByteBuffer.allocate(4 + nameBytes.length + image.length);
            buffer.putInt(name.length());
            buffer.put(nameBytes);
            buffer.put(image);
            buffer.flip();
            getSession().getRemote().sendBytes(buffer);
        } catch (WebSocketException e) {
            if (!e.getMessage().contains("Session closed")) {
                throw e;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
