import ch.trick17.gui.web.WebGui;
import java.util.ArrayList;

public class Squares {

    public static void main(String[] args) {
        WebGui gui = new WebGui("Squares", 800, 600);
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
