import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;
import ch.trick17.gui.widget.Button;
import ch.trick17.gui.widget.Label;

import java.util.Random;

public class Widgets {

    public static void main(String[] args) {
        var gui = Gui.create("Widgets", 800, 600);
        var random = new Random();

        gui.addComponent(new Button("Click me!", 350, 285, 100, 30) {
            public void onLeftClick(double _x, double _y) {
                var x = random.nextInt((int) (gui.getWidth() - 100));
                var y = random.nextInt((int) (gui.getHeight() - 30));
                var button = new Button("Click me too!", x, y, 200, 60) {
                    public void onLeftClick(double x1, double y1) {
                        setText("Done.");
                    }
                };
                gui.addComponent(button);
            }
        });

        var label = new Label("← Apparently, you should click that...", 460, 285, 30);
        label.setTextColor(new Color(200, 200, 200));
        gui.addComponent(label);

        gui.open();
        gui.runUntilClosed(20);
    }
}
