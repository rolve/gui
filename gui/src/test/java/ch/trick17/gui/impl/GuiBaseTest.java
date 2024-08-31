package ch.trick17.gui.impl;

import ch.trick17.gui.Gui;
import ch.trick17.gui.component.Clickable;
import ch.trick17.gui.component.Rectangle;
import ch.trick17.gui.component.Shape;
import ch.trick17.gui.impl.GuiBase.MouseInput;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiBaseTest {

    @Test
    void addComponent() {
        var gui = new TestGui();

        var clicked = new HashSet<String>();
        gui.addComponent(new Clickable() {
            @Override
            public void onLeftClick(double x, double y) {
                clicked.add("left: " + x + ", " + y);
            }

            @Override
            public void onRightClick(double x, double y) {
                clicked.add("right: " + x + ", " + y);
            }

            @Override
            public Shape getInteractiveArea(Gui gui) {
                return new Rectangle(50, 50, 100, 100);
            }
        });

        // outside the clickable area
        gui.mouseX = 10;
        gui.mouseY = 10;
        gui.releasedInputs.add(new MouseInput(true));
        gui.refresh();
        assertTrue(clicked.isEmpty());

        // inside the clickable area
        gui.mouseX = 75;
        gui.mouseY = 75;
        gui.releasedInputs.add(new MouseInput(true));
        gui.refresh();
        assertEquals(1, clicked.size());
        assertTrue(clicked.contains("left: 75.0, 75.0"));

        clicked.clear();
        gui.releasedInputs.add(new MouseInput(false));
        gui.refresh();
        assertEquals(1, clicked.size());
        assertTrue(clicked.contains("right: 75.0, 75.0"));
    }

    static class TestGui extends GuiBase {
        public TestGui() {
            super("Test", 800, 600);
        }
        protected void repaint(boolean clear) {}
        public void setResizable(boolean resizable) {}
        public double stringWidth(String string) { return 0; }
        public void drawRect(double x, double y, double width, double height) {}
        public void fillRect(double x, double y, double width, double height) {}
        public void drawOval(double x, double y, double width, double height) {}
        public void fillOval(double x, double y, double width, double height) {}
        public void drawLine(double x1, double y1, double x2, double y2) {}
        public void drawPath(double[] coordinates) {}
        public void drawPolygon(double[] coordinates) {}
        public void fillPolygon(double[] coordinates) {}
        public void drawMultiPolygon(double[][] rings) {}
        public void fillMultiPolygon(double[][] rings) {}
        public void drawString(String string, double x, double y) {}
        public void drawImage(String path, double x, double y, double scale, double angle) {}
        public void drawImageCentered(String path, double x, double y, double scale, double angle) {}
    }
}
