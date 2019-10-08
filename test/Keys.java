import static java.lang.Math.cos;
import static java.lang.Math.sin;

import gui.Window;

public class Keys {

    public static void main(String[] args) {
        Window window = new Window("Keys", 700, 300);
        window.setResizable(true);
        window.open();

        int speed = 5;
        double x = 100;
        double y = 100;
        int color = 0;
        while (window.isOpen()) {
            if (window.isKeyPressed("w") || window.isKeyPressed("up")) {
                y -= speed;
            }
            if (window.isKeyPressed("s") || window.isKeyPressed("down")) {
                y += speed;
            }
            if (window.isKeyPressed("a") || window.isKeyPressed("left")) {
                x -= speed;
            }
            if (window.isKeyPressed("d") || window.isKeyPressed("right")) {
                x += speed;
            }

            if (window.wasKeyTyped("space")) {
                color++;
            }

            window.setColor((int) (sin(color) * 128 + 64), (int) (cos(color * 1.5) * 128 + 64),
                    (int) (sin(color / 1.5) * 128 + 64));
            window.fillRect(x, y, 50, 50);
            window.fillRect(500, 100, 50, 50);

            window.drawString(window.getPressedKeys().toString(), 20, 50);

            window.refreshAndClear(20);
        }
    }
}
