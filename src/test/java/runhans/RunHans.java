package runhans;

import gui.Gui;
import gui.component.Drawable;

import java.util.ArrayList;

public class RunHans implements Drawable {

    public static final int WIDTH = 960;
    public static final int HEIGHT = 640;
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

    private final Gui gui;

    public RunHans() {
        time = 0;
        hans = new Hans(WIDTH / 2.0, HEIGHT / 2.0);
        enemies = new ArrayList<>();
        level = 0;

        gui = Gui.create("Run, Hans!", WIDTH, HEIGHT);
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
    public void draw(Gui gui) {
        gui.drawImage("img/background.png", 0, 0);

        String levelFile = String.format("img/levels/%02d.png", level);
        gui.drawImageCentered(levelFile, 96, HEIGHT - 32);

        if (gameOver) {
            gui.drawImageCentered("img/gameover.png", WIDTH/2, HEIGHT/2);
        }
    }
}
