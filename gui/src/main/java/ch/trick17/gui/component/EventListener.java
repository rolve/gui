package ch.trick17.gui.component;

import ch.trick17.gui.Gui;

/**
 * A {@link Component} that can react to any event that occurs in the GUI. If
 * you only want to react to local mouse events, consider implementing the
 * simpler {@link Clickable} or {@link Hoverable} instead.
 */
public interface EventListener extends Component {
    default void onKeyPress(String key) {}
    default void onKeyRelease(String key) {}
    default void onMouseButtonPress(double x, double y, boolean left) {}
    default void onMouseButtonRelease(double x, double y, boolean left) {}
    // TODO: onMouseMove
}
