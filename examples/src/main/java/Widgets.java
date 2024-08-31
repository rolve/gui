import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;
import ch.trick17.gui.widget.Button;
import ch.trick17.gui.widget.Label;
import ch.trick17.gui.widget.TextField;

import java.util.Random;

import static java.lang.Double.parseDouble;

public class Widgets {

    private static final Color ERROR_COLOR = new Color(200, 0, 0);

    private static double buttonSize;

    public static void main(String[] args) {
        var gui = Gui.create("Widgets", 800, 600);
        var random = new Random();

        var textFieldLabel = new Label("Size of new buttons:", 10, 10, 30);
        var textField = new TextField(10, 10 + textFieldLabel.getHeight(), 200, 30) {
            protected void onTextChange(String text) {
                try {
                    var size = parseDouble(text);
                    if (size < 0 || size > 100) {
                        throw new NumberFormatException();
                    }
                    buttonSize = size;
                    setBorderColor(DEFAULT_BORDER_COLOR);
                    setFocussedBorderColor(DEFAULT_FOCUSSED_BORDER_COLOR);
                } catch (NumberFormatException e) {
                    setBorderColor(ERROR_COLOR);
                    setFocussedBorderColor(ERROR_COLOR);
                }
            }
        };
        textField.setText("60");

        var button = new Button("Click me!", 350, 285, 100, 30) {
            public void onLeftClick(double _x, double _y) {
                var width = 4 * buttonSize;
                var height = buttonSize;
                var x = random.nextDouble() * (gui.getWidth() - width / 2);
                var y = random.nextDouble() * (gui.getHeight() - height / 2);
                var newButton = new Button("Click me too!", x, y, width, height) {
                    public void onLeftClick(double x1, double y1) {
                        setText("Done.");
                    }
                };
                gui.addComponent(newButton);
            }
        };

        var buttonLabel = new Label("← Apparently, you should click that...", 460, 285, 30);
        buttonLabel.setTextColor(new Color(200, 200, 200));

        gui.addComponents(textFieldLabel, textField, button, buttonLabel);
        gui.open();
        gui.runUntilClosed(20);
    }
}
