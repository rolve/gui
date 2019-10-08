package gui.component;

import gui.Window;

/**
 * A {@link Component} that can be drawn on a {@link Window}. It has a single
 * method, {@link #draw(Window)}, which takes the window instance on which it
 * should be drawn.
 */
public interface Drawable extends Component {
    void draw(Window window);
}