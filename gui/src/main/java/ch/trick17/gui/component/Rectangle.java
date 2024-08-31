package ch.trick17.gui.component;

import ch.trick17.gui.Gui;

/**
 * A simple 2D rectangle that can be used as the
 * {@linkplain Interactive#getInteractiveArea(Gui) interactive area} of a
 * component.
 */
public class Rectangle implements Shape {

    private final double x, y, width, height;

    /**
     * Creates a rectangle of size (width, height) with the upper left corner at
     * coordinate (x, y).
     */
    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double px, double py) {
        return px >= x && py >= y && px <= x + width && py <= y + height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}