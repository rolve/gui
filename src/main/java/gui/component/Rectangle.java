package gui.component;

/**
 * A simple 2D rectangle. Used by {@link Interactive#getBoundingBox()} to define
 * the interactive area of a component.
 */
public class Rectangle {
    public final double x, y, width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double px, double py) {
        return px >= x && py >= y && px <= x + width && py <= y + height;
    }
}