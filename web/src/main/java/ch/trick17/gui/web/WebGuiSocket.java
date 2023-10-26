package ch.trick17.gui.web;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;

public class WebGuiSocket extends WebSocketAdapter {

    private int x = 400;
    private int y = 300;
    private int size = 100;

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        render();
    }

    @Override
    public void onWebSocketText(String message) {
        switch (message) {
            case "keyDown  ArrowLeft":
                x -= 10;
                break;
            case "keyDown  ArrowRight":
                x += 10;
                break;
            case "keyDown  ArrowUp":
                y -= 10;
                break;
            case "keyDown  ArrowDown":
                y += 10;
                break;
            case "keyDown  +":
                size += 10;
                break;
            case "keyDown  -":
                size -= 10;
                break;
            default:
                System.out.println("Unknown message: " + message);
                return;
        }
        render();
    }

    private void render() {
        try {
            getSession().getRemote().sendString(
                    "clear   \n" +
                    "drawRect " + (x - size / 2) + "," + (y - size / 2) + ","
                    + size + "," + size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
