import ch.trick17.gui.web.WebGui;

import static java.lang.Math.max;

public class Mouse {

    public static void main(String[] args) {
        WebGui gui = new WebGui("Mouse", 700, 300);
        gui.setColor(0, 128, 0);

        double x = 50;
        double y = 50;
        double size = 16;
        while (gui.isOpen()) {
            if (gui.wasLeftMouseButtonClicked()) {
                size *= 2;
            }
            if (gui.isRightMouseButtonPressed()) {
                size = max(size / 2, 1);
            }

            double xDiff = x - gui.getMouseX();
            double yDiff = y - gui.getMouseY();
            x -= xDiff / size;
            y -= yDiff / size;
            gui.fillRect(x - size / 2, y - size / 2, size + 1, size + 1);
            gui.refreshAndClear(20);
        }
    }
}
