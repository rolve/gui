import ch.trick17.gui.web.WebGui;

public class Pixels {
    public static void main(String[] args) {
        WebGui gui = new WebGui("Pixels", 255, 255);
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
