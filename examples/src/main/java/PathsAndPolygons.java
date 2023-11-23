import gui.Color;
import gui.Gui;

public class PathsAndPolygons {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
        Gui gui = Gui.create("Paths & Polygons", WIDTH, HEIGHT);

        var path = new double[] {
                380,  40,
                220, 200,
                380, 360};
        gui.setStrokeWidth(3);
        gui.drawPath(path);

        var bigK = new double[] {
                 20,  20,
                380,  20,
                200, 200,
                380, 380,
                 20, 380};
        gui.setColor(150, 200, 250);
        gui.fillPolygon(bigK);

        gui.setColor(100, 150, 200);
        gui.setStrokeWidth(5);
        gui.setRoundStroke(true);
        gui.drawPolygon(bigK);

        var selfIntersecting = new double[] {
                 50, 100,
                250, 100,
                100, 250,
                100, 150,
                250, 300,
                 50, 300};
        gui.setColor(new Color(255, 255, 255, 100));
        gui.fillPolygon(selfIntersecting);

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
        gui.setColor(150, 200, 250);
        gui.fillMultiPolygon(withHole);

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
        gui.setColor(0, 100, 150);
        gui.fillMultiPolygon(intersecting);

        gui.open();
        gui.waitUntilClosed();
    }
}
