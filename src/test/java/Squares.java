import gui.Gui;

import java.util.ArrayList;

public class Squares {

    public static void main(String[] args) {
        Gui gui = Gui.create("Squares", 800, 600);
        gui.open();

        var positions = new ArrayList<double[]>();
        while (gui.isOpen()) {
            if (gui.wasLeftMouseButtonClicked()) {
                positions.add(new double[] {gui.getMouseX(), gui.getMouseY()});
            }

            for (var pos : positions) {
                gui.fillRect(pos[0] - 2.5, pos[1] - 2.5, 5, 5);
            }

            gui.refreshAndClear(20);
        }
    }
}
