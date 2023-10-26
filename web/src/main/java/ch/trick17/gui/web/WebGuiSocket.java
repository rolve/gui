package ch.trick17.gui.web;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Random;

public class WebGuiSocket extends WebSocketAdapter {

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        var random = new Random();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
                session.getRemote().sendString("setColor "
                                               + random.nextInt(256) + ","
                                               + random.nextInt(256) + ","
                                               + random.nextInt(256));
                var w = random.nextInt(200);
                var h = random.nextInt(200);
                var x = random.nextGaussian() * 400 + 400 - w / 2;
                var y = random.nextGaussian() * 300 + 300 - h / 2;
                if (random.nextBoolean()) {
                    session.getRemote().sendString(
                            "drawRect " + x + "," + y + "," + w + "," + h);
                } else {
                    session.getRemote().sendString(
                            "fillRect " + x + "," + y + "," + w + "," + h);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
