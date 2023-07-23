import gui.Color;
import gui.Window;

public class PathsAndPolygons {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
        Window window = new Window("Paths & Polygons", WIDTH, HEIGHT);

        var path = new double[] {
                380,  40,
                220, 200,
                380, 360};

        window.setStrokeWidth(3);
        window.drawPath(path);

        var bigK = new double[] {
                 20,  20,
                380,  20,
                200, 200,
                380, 380,
                 20, 380};

        window.setColor(150, 200, 250);
        window.fillPolygon(bigK);

        window.setColor(100, 150, 200);
        window.setStrokeWidth(5);
        window.setRoundStroke(true);
        window.drawPolygon(bigK);

        var withHole = new double[] {
                 50, 100,
                250, 100,
                100, 250,
                100, 150,
                250, 300,
                 50, 300};

        window.setColor(new Color(255, 255, 255, 100));
        window.fillPolygon(withHole);

        window.open();
        window.waitUntilClosed();
    }
}
