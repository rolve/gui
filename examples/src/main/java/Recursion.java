import ch.trick17.gui.Gui;

public class Recursion {

    private static final int SIZE = 500;

    public static void main(String[] args) {
        new Recursion().run();
    }

    private final Gui gui = Gui.create("Recursion", SIZE, SIZE);
    private int time;

    private void run() {
        gui.open();
        gui.setRoundStroke(true);

        time = 0;
        while (gui.isOpen()) {
            drawTree(SIZE / 2, SIZE, 100, Math.PI / 2);
            gui.refreshAndClear(20);
            time++;
        }
    }

    public void drawTree(double x, double y, double len, double angle) {
        if (len < 3) {
            gui.setColor(0, 200, 0);
            gui.fillCircle(x, y, 1.5);
            return;
        }

        double timeOffset = Math.sin(time / 20.0) * Math.cos(time / 15.0) * 1 / len / 2;

        double nx = x + len * Math.cos(angle);
        double ny = y - len * Math.sin(angle);

        gui.setStrokeWidth(len / 10);
        gui.setColor(0, 0, 0);
        gui.drawLine(x, y, nx, ny);

        drawTree(nx, ny, len * 0.8, angle + 0.02 + timeOffset);
        drawTree(nx, ny, len * 0.375, angle + Math.PI / 5 + timeOffset);
        drawTree(nx, ny, len * 0.460, angle - Math.PI / 3 + timeOffset);
    }
}
