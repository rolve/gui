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

        var selfIntersecting = new double[] {
                 50, 100,
                250, 100,
                100, 250,
                100, 150,
                250, 300,
                 50, 300};
        window.setColor(new Color(255, 255, 255, 100));
        window.fillPolygon(selfIntersecting);

        var withHole = new double[][]{
                {
                        250, 200,
                        300, 150,
                        350, 200,
                        300, 250
                },
                {
                        280, 200,
                        300, 160,
                        320, 200,
                        300, 240
                }};
        window.setColor(150, 200, 250);
        window.fillMultiPolygon(withHole);

        var intersecting = new double[][]{
                {
                         50,  50,
                        100,  50,
                        100,  70,
                         50,  70
                },
                {
                         60,  60,
                        110,  60,
                        110,  80,
                         60,  80
                }};
        window.setColor(0, 100, 150);
        window.fillMultiPolygon(intersecting);

        window.open();
        window.waitUntilClosed();
    }
}
