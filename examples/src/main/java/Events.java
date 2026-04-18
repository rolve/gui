import ch.trick17.gui.Gui;
import ch.trick17.gui.component.Drawable;
import ch.trick17.gui.component.EventListener;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class Events {

    final List<Line2D.Double> lines = new ArrayList<>();
    final Gui gui = Gui.create("Events", 400, 300);

    public static void main(String[] args) {
        new Events().run();
    }

    private void run() {
        gui.addComponent(new DrawingComponent());
        gui.open();
        gui.runUntilClosed(10);
    }

    class DrawingComponent implements Drawable, EventListener {
        @Override
        public void draw(Gui gui) {
            for (var line : lines) {
                gui.drawLine(line.x1, line.y1, line.x2, line.y2);
            }
        }

        @Override
        public void onMouseMove(double x, double y, double prevX, double prevY) {
            if (gui.isLeftMouseButtonPressed()) {
                lines.add(new Line2D.Double(prevX, prevY, x, y));
            }
        }
    }
}
