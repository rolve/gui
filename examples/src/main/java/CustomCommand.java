import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import ch.trick17.gui.Color;
import ch.trick17.gui.impl.swing.Window;

public class CustomCommand {

    public static void main(String[] args) throws NoninvertibleTransformException {
        var gui = new Window("Custom Command", 500, 300);
        gui.open();

        double rotationAngle = Math.PI / 4;

        while (gui.isOpen()) {
            if (rotationAngle < 2 * Math.PI) rotationAngle += 0.05;
            else rotationAngle = 0;

            int blueRectWidth = 100;
            int blueRectHeight = 100;
            int blueRectX = 100;
            int blueRectY = 100;

            var transform = AffineTransform.getRotateInstance(rotationAngle, blueRectX + blueRectWidth / 2.0, blueRectY + blueRectHeight / 2.0);
            gui.addCustomCommand(g -> g.transform(transform));
            gui.setColor(new Color(100, 100, 255));
            gui.fillRect(blueRectX, blueRectY, blueRectWidth, blueRectHeight);

            // Create the inverse of the rotation matrix to reverse this rotation and draw the next rectangle normally
            var inverse = transform.createInverse();
            gui.addCustomCommand(g -> g.transform(inverse));
            gui.setColor(new Color(255, 100, 100));
            gui.fillRect(300, 100, 100, 100);

            gui.refreshAndClear(20);
        }
    }
}
