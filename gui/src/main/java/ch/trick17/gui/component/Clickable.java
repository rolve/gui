package ch.trick17.gui.component;

import ch.trick17.gui.Gui;

/**
 * A {@link Component} that may react when the user clicks on it. The methods
 * {@link #onLeftClick(double, double)} and/or
 * {@link #onRightClick(double, double)} are called with the precise mouse
 * coordinates when a click occurs inside the area defined by
 * {@link #getInteractiveArea(Gui)}.
 */
public interface Clickable extends Interactive {
    void onLeftClick(double x, double y);
    void onRightClick(double x, double y);
}
