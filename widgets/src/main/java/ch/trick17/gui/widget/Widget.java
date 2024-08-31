package ch.trick17.gui.widget;

import ch.trick17.gui.component.Drawable;

public abstract class Widget implements Drawable {

    private double x;
    private double y;

    protected Widget(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
