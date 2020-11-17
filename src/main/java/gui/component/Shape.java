package gui.component;

/**
 * A shape that defines the interactive area of a component. Used by
 * {@link Interactive#getInteractiveArea(gui.Window)}.
 *
 * @see Rectangle
 */
public interface Shape {

    /**
     * Returns <code>true</code> if the given coordinates lie inside this shape.
     */
    boolean contains(double px, double py);
}
