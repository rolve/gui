import ch.trick17.gui.Gui;

public class Pixels {
    public static void main(String[] args) {
        Gui gui = Gui.create("Pixels", 255, 255);
        for (int x = 0; x < 255; x++) {
            for (int y = 0; y < 255; y++) {
                gui.setColor(127, x, y);
                gui.fillRect(x, y, 1, 1);
            }
        }
        gui.open();
        gui.waitUntilClosed();
    }
}
