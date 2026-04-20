package ch.trick17.gui.component;

/**
 * A {@link Component} that can react to any event that occurs in the GUI. If
 * you only want to react to local mouse events, consider implementing the
 * simpler {@link Clickable} or {@link Hoverable} instead.
 */
public interface EventListener extends Component {
    default void onKeyPress(String keyName, char keyChar) {}
    default void onKeyRelease(String keyName, char keyChar) {}
    default void onMouseButtonPress(double x, double y, boolean left) {}
    default void onMouseButtonRelease(double x, double y, boolean left) {}

    /**
     * Called whenever the mouse moves. This method is never called with
     * <code>x == prevX &amp;&amp; y == prevY</code>.
     *
     * @param x     the new x coordinate of the mouse, where 0 ≤ x &lt; width of
     *              the GUI
     * @param y     the new y coordinate of the mouse, where 0 ≤ y &lt; height of
     *              the GUI
     * @param prevX the previous x coordinate of the mouse, guaranteed to be the
     *              same as the <code>x</code> value of the last time this
     *              method was called
     * @param prevY the previous y coordinate of the mouse, guaranteed to be the
     *              same as the <code>y</code> value of the last time this
     *              method was called
     */
    default void onMouseMove(double x, double y, double prevX, double prevY) {}
}
