import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;

public class Alpha {

    public static void main(String[] args) {
        Gui gui = Gui.create("Alpha", 200, 200);

        // first option: use color with alpha value:
        for (int i = 0; i < 5; i++) {
            gui.setColor(new Color(255, 255, 255, 127));
            gui.fillRect(0, 0, 200, 200);
            gui.setColor(255, 0, 0);
            gui.fillRect(30 + i * 20, 30 + i * 20, 50, 50);
        }

        // second option: use alpha setting for all drawing commands:
        gui.setAlpha(0.5);
        gui.setColor(new Color(0, 127, 0));
        gui.setStrokeWidth(5);
        gui.drawCircle(150, 150, 30);

        gui.open();
        gui.waitUntilClosed();
    }
}
