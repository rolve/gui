package gui.component;

import gui.Gui;

/**
 * A shape that defines the interactive area of a component. Used by
 * {@link Interactive#getInteractiveArea(Gui)}.
 *
 * @see Rectangle
 */
public interface Shape {

    /**
     * Returns <code>true</code> if the given coordinates lie inside this shape.
     */
    boolean contains(double px, double py);
}
