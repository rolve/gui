package ch.trick17.gui.web;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;

import static java.lang.Double.parseDouble;

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
    public void onWebSocketText(String event) {
        var name = event.substring(0, 8);
        var args = event.substring(9);
        switch (name) {
            case "keyDown ":
                handleKeyDown(args);
                break;
            case "keyUp   ":
                handleKeyUp(args);
                break;
            case "mouseUp ":
                handleMouseUp(args);
                break;
            case "mouseDwn":
                handleMouseDown(args);
                break;
            case "mouseMov":
                var parts = args.split(",");
                handleMouseMove(parseDouble(parts[0]), parseDouble(parts[1]));
                break;
            default:
                System.out.println("Unknown event: " + event);
        }
    }

    private void handleKeyDown(String key) {
        switch (key) {
            case "ArrowLeft":
                x -= 10;
                break;
            case "ArrowRight":
                x += 10;
                break;
            case "ArrowUp":
                y -= 10;
                break;
            case "ArrowDown":
                y += 10;
                break;
            case "+":
                size += 10;
                break;
            case "-":
                size -= 10;
                break;
        }
        render();
    }

    private void handleKeyUp(String key) {
        // nothing to do
    }

    private void handleMouseMove(String button) {
        // nothing to do
    }

    private void handleMouseUp(String button) {
        // nothing to do
    }

    private void handleMouseDown(String button) {
        // nothing to do
    }

    private void handleMouseMove(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        render();
    }

    private void render() {
        try {
            getSession().getRemote().sendString(
                    "clear    \n" +
                    "drawRect " + (x - size / 2) + "," + (y - size / 2) + ","
                    + size + "," + size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
