package gui.component;

import gui.Window;

/**
 * A {@link Component} that is interactive. This is a common supertype for the
 * two interfaces {@link Hoverable} and {@link Clickable}. It declares the
 * {@link #getInteractiveArea(Window)} method, which defines the area of
 * interaction as a {@link Shape}.
 */
public interface Interactive extends Component {

    /**
     * Defines the area in which this component reacts to events such as mouse
     * clicks.
     */
    Shape getInteractiveArea(Window window);
}
