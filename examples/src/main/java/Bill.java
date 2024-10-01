import ch.trick17.gui.Gui;

import java.util.Random;

import static java.lang.Math.atan2;
import static java.lang.Math.max;

public class Bill {

    private static final int WIDTH = 658;
    private static final int HEIGHT = 432;
    private static final double BILL_SIZE = 64;
    private static final double COIN_SIZE = 16;

    public static void main(String[] args) {
        new Bill().run();
    }

    private final Random random = new Random(42);

    private void run() {
        Gui gui = Gui.create("Bill", WIDTH, HEIGHT);
        gui.setFontSize(16);
        gui.open();

        int t = 0;

        double x = WIDTH / 2.0;
        double y = HEIGHT / 2.0;
        double vx = 0;
        double vy = 0;
        double acc = 0.15;

        double coinX = randomX();
        double coinY = randomY();

        int score = 0;
        int highscore = 0;
        int lives = 3;

        while (gui.isOpen()) {
            if (gui.isKeyPressed("w") || gui.isKeyPressed("up")) {
                vy -= acc;
            }
            if (gui.isKeyPressed("s") || gui.isKeyPressed("down")) {
                vy += acc;
            }
            if (gui.isKeyPressed("a") || gui.isKeyPressed("left")) {
                vx -= acc;
            }
            if (gui.isKeyPressed("d") || gui.isKeyPressed("right")) {
                vx += acc;
            }

            if (gui.wasKeyTyped("space")) {
                lives = -1;
            }

            if (gui.wasKeyTyped("f")) {
                try {
                    gui.setFullScreen(!gui.isFullScreen());
                } catch (UnsupportedOperationException e) {
                    // ignore
                }
            }

            if (x < BILL_SIZE / 2 || x + BILL_SIZE / 2 >= WIDTH) {
                vx = -vx;
                lives--;
            }
            if (y < BILL_SIZE / 2 || y + BILL_SIZE / 2 >= HEIGHT) {
                vy = -vy;
                lives--;
            }

            x += vx;
            y += vy;

            double coinDist = (x - coinX) * (x - coinX) + (y - coinY) * (y - coinY);
            if (coinDist < (BILL_SIZE + COIN_SIZE) * (BILL_SIZE + COIN_SIZE) / 4) {
                score++;
                coinX = randomX();
                coinY = randomY();
            }

            if (lives < 0) {
                highscore = max(highscore, score);
                score = 0;
                lives = 3;
                x = WIDTH / 2.0;
                y = HEIGHT / 2.0;
                vx = vy = 0;
            }

            int coinSprite = max(0, t / 3 % 40 - 35);
            gui.drawImage("img/bill/background.png", 0, 0, 0.5);
            gui.drawImageCentered("img/bill/coin" + coinSprite + ".png", coinX, coinY, 0.5);
            gui.drawImageCentered("img/bill/bill.png", (int) x, (int) y, 0.5, atan2(vy, vx));

            gui.setColor(255, 255, 255);
            gui.drawString("Score: " + score + "  Lives: " + lives, 10, 25);
            gui.setColor(200, 200, 0);
            gui.drawString("Highscore: " + highscore, 10, 45);

            gui.refreshAndClear(20);
            t++;
        }
    }

    private double randomX() {
        return random.nextDouble() * (WIDTH - 2 * BILL_SIZE) + BILL_SIZE;
    }

    private double randomY() {
        return random.nextDouble() * (HEIGHT - 2 * BILL_SIZE) + BILL_SIZE;
    }
}
