import ch.trick17.gui.web.WebGui;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Keys {

    public static void main(String[] args) {
        WebGui gui = new WebGui("Keys", 700, 300);

        int speed = 5;
        double x = 100;
        double y = 100;
        int color = 0;
        while (gui.isOpen()) {
            if (gui.isKeyPressed("w") || gui.isKeyPressed("up")) {
                y -= speed;
            }
            if (gui.isKeyPressed("s") || gui.isKeyPressed("down")) {
                y += speed;
            }
            if (gui.isKeyPressed("a") || gui.isKeyPressed("left")) {
                x -= speed;
            }
            if (gui.isKeyPressed("d") || gui.isKeyPressed("right")) {
                x += speed;
            }

            if (gui.wasKeyTyped("space")) {
                color++;
            }

            gui.setColor((int) (sin(color) * 128 + 64), (int) (cos(color * 1.5) * 128 + 64),
                    (int) (sin(color / 1.5) * 128 + 64));
            gui.fillRect(x, y, 50, 50);
            gui.fillRect(500, 100, 50, 50);

            gui.refreshAndClear(20);
        }
    }
}
