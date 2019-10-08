import static java.lang.Math.atan2;
import static java.lang.Math.max;

import java.util.Random;

public class Bill {

    private static final int width = 658;
    private static final int height = 432;
    private static final int billSize = 64;
    private static final int coinSize = 16;

    private static Random random = new Random(42);

    public static void main(String[] args) {
        Window window = new Window("Bill", width, height);
        window.setFontSize((int) (window.getFontSize() * 1.5));
        window.open();

        int t = 0;

        double x = width / 2;
        double y = height / 2;
        double vx = 0;
        double vy = 0;
        double acc = 0.15;

        int coinX = randomX();
        int coinY = randomY();

        int score = 0;
        int highscore = 0;
        int lives = 3;

        while (window.isOpen()) {
            if (window.isKeyPressed("w") || window.isKeyPressed("up")) {
                vy -= acc;
            }
            if (window.isKeyPressed("s") || window.isKeyPressed("down")) {
                vy += acc;
            }
            if (window.isKeyPressed("a") || window.isKeyPressed("left")) {
                vx -= acc;
            }
            if (window.isKeyPressed("d") || window.isKeyPressed("right")) {
                vx += acc;
            }

            if (window.wasKeyTyped("space")) {
                lives = -1;
            }

            if (x < billSize / 2 || x + billSize / 2 >= width) {
                vx = -vx;
                lives--;
            }
            if (y < billSize / 2 || y + billSize / 2 >= height) {
                vy = -vy;
                lives--;
            }

            x += vx;
            y += vy;

            double coinDist = (x - coinX) * (x - coinX) + (y - coinY) * (y - coinY);
            if (coinDist < (billSize + coinSize) * (billSize + coinSize) / 4) {
                score++;
                coinX = randomX();
                coinY = randomY();
            }

            if (lives < 0) {
                highscore = max(highscore, score);
                score = 0;
                lives = 3;
                x = width / 2;
                y = height / 2;
                vx = vy = 0;
            }

            int coinSprite = max(0, t / 3 % 40 - 35);
            window.drawImage("background.png", 0, 0);
            window.drawImageCentered("coin" + coinSprite + ".png", coinX, coinY);
            window.drawImageCentered("bill.png", (int) x, (int) y, 1, atan2(vy, vx));

            window.setColor(255, 255, 255);
            window.drawString("Score: " + score + "  Lives: " + lives, 10, 25);
            window.setColor(200, 200, 0);
            window.drawString("Highscore: " + highscore, 10, 45);

            window.refreshAndClear(20);
            t++;
        }
    }

    private static int randomX() {
        return random.nextInt(width - 2 * billSize) + billSize;
    }

    private static int randomY() {
        return random.nextInt(height - 2 * billSize) + billSize;
    }
}
