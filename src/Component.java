/**
 * A component is an object that can be drawn and/or interacted with in a
 * {@link Window}. This interface does not declare any method and is only there
 * to have a common supertype for the interfaces {@link Drawable} and
 * {@link Interactive}.
 */
public interface Component {}

/**
 * A {@link Component} that can be drawn on a {@link Window}. It has a single
 * method, {@link #draw(Window)}, which takes the window instance on which it
 * should be drawn.
 */
interface Drawable extends Component {
    void draw(Window window);
}

/**
 * A {@link Component} that is interactive. This is a common supertype for the
 * two interfaces {@link Hoverable} and {@link Clickable}. It declares the
 * {@link #getBoundingBox()} method, which defines the area of interaction as a
 * {@link Rectangle}.
 */
interface Interactive extends Component {
    Rectangle getBoundingBox();
}

/**
 * A {@link Component} that may react when the user moves the mouse over it. The
 * {@link #onMouseEnter()} method is called when the mouse pointer enters the
 * area defined by {@link #getBoundingBox()} and {@link #onMouseExit()} is
 * called when the mouse pointer leaves it.
 */
interface Hoverable extends Interactive {
    void onMouseEnter();
    void onMouseExit();
}

/**
 * A {@link Component} that may react when the user clicks on it. The methods
 * {@link #onLeftClick(double, double)} and/or
 * {@link #onRightClick(double, double)} are called with the precise mouse
 * coordinates when a click occurs inside the area define by
 * {@link #getBoundingBox()}.
 */
interface Clickable extends Interactive {
    void onLeftClick(double x, double y);
    void onRightClick(double x, double y);
}

/**
 * A simple 2D rectangle. Used by {@link Interactive#getBoundingBox()} to define
 * the interactive area of a component.
 */
class Rectangle {
    final double x, y, width, height;
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