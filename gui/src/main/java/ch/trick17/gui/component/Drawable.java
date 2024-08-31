package ch.trick17.gui.component;

import ch.trick17.gui.Gui;

/**
 * A {@link Component} that can be drawn on a {@link Gui}. It has a single
 * method, {@link #draw(Gui)}, which takes the GUI instance on which it
 * should be drawn.
 */
public interface Drawable extends Component {
    void draw(Gui gui);
}