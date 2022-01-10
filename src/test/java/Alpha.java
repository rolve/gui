import gui.Color;
import gui.Window;

public class Alpha {

    public static void main(String[] args) {
        Window window = new Window("Alpha", 200, 200);
        for (int i = 0; i < 5; i++) {
            window.setColor(new Color(255, 255, 255, 127));
            window.fillRect(0, 0, 200, 200);
            window.setColor(255, 0, 0);
            window.fillRect(30 + i * 20, 30 + i * 20, 50, 50);
        }

        window.setColor(new Color(0, 127, 0, 127));
        window.setStrokeWidth(5);
        window.drawCircle(150, 150, 30);

        window.open();
        window.waitUntilClosed();
    }
}
