import static java.lang.Math.max;

import gui.Gui;

public class Mouse {

    public static void main(String[] args) {
        Gui gui = Gui.create("Mouse", 700, 300);
        gui.setColor(0, 128, 0);
        gui.open();

        double x = 50;
        double y = 50;
        int size = 16;
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
            gui.drawLine(gui.getMouseX(), gui.getMouseY(), x, y);
            gui.fillOval(x - size / 2, y - size / 2, size + 1, size + 1);
            gui.drawString((int) x + ", " + (int) y, x, y - size / 2 - size / 10 - 2);
            gui.refreshAndClear(20);
        }
    }
}
