package gui.impl.web;

import jakarta.websocket.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.lang.String.join;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WebGuiEndpoint extends Endpoint implements MessageHandler.Whole<String> {

    private static Method mainMethod;
    private static final ThreadLocal<WebGuiEndpoint> current = new ThreadLocal<>();

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
    private Session session;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(this);
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
    public void onMessage(String message) {
        gui.onEvent(message);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        gui.close();
    }

    void close() {
        try {
            session.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void send(Iterable<? extends CharSequence> commands) {
        try {
            session.getBasicRemote().sendText(join("\n", commands));
        } catch (IOException e) {
            if (!(e.getCause() instanceof ClosedChannelException)) {
                throw new UncheckedIOException(e);
            }
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
            session.getBasicRemote().sendBinary(buffer);
        } catch (IOException e) {
            if (!(e.getCause() instanceof ClosedChannelException)) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
