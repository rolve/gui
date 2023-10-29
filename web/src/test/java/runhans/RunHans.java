package runhans;

import ch.trick17.gui.web.WebGui;
import ch.trick17.gui.web.component.Drawable;

import java.util.ArrayList;

public class RunHans implements Drawable {

    public static final int PIXEL_WIDTH = 480;
    public static final int PIXEL_HEIGHT = 320;
    public static final int PIXEL_SCALE = 2;
    public static final int WIDTH = PIXEL_WIDTH * PIXEL_SCALE;
    public static final int HEIGHT = PIXEL_HEIGHT * PIXEL_SCALE;
    public static final double FRAME_RATE = 50;

    private static final double ENEMIES_PER_MINUTE = 15;
    private static final int MAX_LEVEL = 50;

    public static void main(String[] args) {
        new RunHans().play();
    }

    private double time; // seconds
    private final Hans hans;
    private final ArrayList<Enemy> enemies;
    private int level;
    private boolean gameOver = false;

    private final WebGui gui;

    public RunHans() {
        time = 0;
        hans = new Hans(WIDTH / 2.0, HEIGHT / 2.0);
        enemies = new ArrayList<>();
        level = 0;

        gui = new WebGui("Run, Hans!", WIDTH, HEIGHT);
        gui.addComponent(this);
        gui.addComponent(hans);
        checkLevelUp(); // add first enemy, increase level to 1
    }

    public void play() {
        gui.open();
        while (gui.isOpen() && !gameOver) {
            step();
            if (level < MAX_LEVEL) {
                checkLevelUp();
            }

            time += 1.0/FRAME_RATE;
            gui.refreshAndClear((int) (1000 / FRAME_RATE));
        }

        gui.waitUntilClosed();
    }

    private void step() {
        hans.step(gui);
        for (Enemy enemy : enemies) {
            if (enemy.distanceTo(hans) <= 0) {
                gameOver = true;
            }
            enemy.step(hans);
        }
    }

    private void checkLevelUp() {
        if (level <= time * ENEMIES_PER_MINUTE / 60) {
            double x = Math.random() * WIDTH;
            double y = Math.random() * HEIGHT;
            int enemyKind = (int) (Math.random() * 5);
            Enemy enemy;
            if (enemyKind == 0) {
                enemy = new Pig(x, y);
            } else if (enemyKind == 1) {
                enemy = new Turtle(x, y);
            } else if (enemyKind == 2) {
                enemy = new Bat(x, y);
            } else if (enemyKind == 3) {
                enemy = new Plant(x, y);
            } else {
                enemy = new Rhino(x, y);
            }
            enemies.add(enemy);
            gui.addComponent(enemy);
            level++;
        }
    }

    @Override
    public void draw(WebGui window) {
        window.drawImage("web/img/background.png", 0, 0, PIXEL_SCALE);

        String levelFile = String.format("web/img/levels/%02d.png", level);
        window.drawImageCentered(levelFile, 96, HEIGHT - 32, 3);

        if (gameOver) {
            window.drawImageCentered("web/img/gameover.png", WIDTH/2, HEIGHT/2, 10);
        }
    }
}
