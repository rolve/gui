package gui.component;

import gui.Gui;

/**
 * A {@link Component} that may react when the user moves the mouse over it. The
 * {@link #onMouseEnter()} method is called when the mouse pointer enters the
 * area defined by {@link #getInteractiveArea(Gui)} and {@link #onMouseExit()} is
 * called when the mouse pointer leaves it.
 */
public interface Hoverable extends Interactive {
    void onMouseEnter();
    void onMouseExit();
}
