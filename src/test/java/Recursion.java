import gui.Window;

public class Recursion {

    private static final int SIZE = 500;
    private static final Window window = new Window("Recursion", SIZE, SIZE);
    private static int time;

    public static void main(String[] args) {
        window.open();

        time = 0;
        while (window.isOpen()) {
            drawTree(SIZE / 2, SIZE, 100, Math.PI / 2);
            window.refreshAndClear(20);
            time++;
        }
    }

    public static void drawTree(double x, double y, double len, double angle) {
        if (len < 3) {
            window.setColor(0, 200, 0);
            window.fillCircle(x, y, 1.5);
            return;
        }

        double timeOffset = Math.sin(time / 20.0) * Math.cos(time / 15.0) * 1 / len / 2;

        double nx = x + len * Math.cos(angle);
        double ny = y - len * Math.sin(angle);

        window.setStrokeWidth(len / 10);
        window.setColor(0, 0, 0);
        window.drawLine(x, y, nx, ny);

        drawTree(nx, ny, len * 0.8, angle + 0.02 + timeOffset);
        drawTree(nx, ny, len * 0.375, angle + Math.PI / 5 + timeOffset);
        drawTree(nx, ny, len * 0.460, angle - Math.PI / 3 + timeOffset);
    }
}
