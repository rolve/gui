package gui.component;

/**
 * A {@link Component} that is interactive. This is a common supertype for the
 * two interfaces {@link Hoverable} and {@link Clickable}. It declares the
 * {@link #getBoundingBox()} method, which defines the area of interaction as a
 * {@link Rectangle}.
 */
public interface Interactive extends Component {
    Rectangle getBoundingBox();
}