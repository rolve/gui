import gui.Color;
import gui.Window;

public class Image {

    public static void main(String[] args) {
        Window window = new Window("Image", 400, 400);
        window.open();

        double angle = 0.0;
        window.setColor(Color.parseHexCode("#FDE70E"));
        while (window.isOpen()) {
            window.fillRect(0, 0, 400, 400);

            double scale = Math.cos(angle) * 0.3 + 0.7;
            // logo.png is loaded from classpath
            window.drawImageCentered("logo.png", 200, 200, scale, angle);

            angle += 0.05;
            window.refreshAndClear(20);
        }
    }
}
