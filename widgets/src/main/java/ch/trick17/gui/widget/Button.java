package ch.trick17.gui.widget;

import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;
import ch.trick17.gui.component.Clickable;
import ch.trick17.gui.component.Hoverable;
import ch.trick17.gui.component.Rectangle;

import static java.util.Objects.requireNonNull;

public abstract class Button extends Widget implements Hoverable, Clickable {

    public static final Color DEFAULT_BACKGROUND_COLOR = new Color(41, 127, 213);
    public static final Color DEFAULT_TEXT_COLOR = new Color(255, 255, 255);
    public static final Color DEFAULT_HOVERED_BACKGROUND_COLOR = new Color(31, 95, 160);
    public static final Color DEFAULT_HOVERED_TEXT_COLOR = DEFAULT_TEXT_COLOR;

    private String text;
    private double width;
    private double height;

    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private Color textColor = DEFAULT_TEXT_COLOR;
    private Color hoveredBackgroundColor = DEFAULT_HOVERED_BACKGROUND_COLOR;
    private Color hoveredTextColor = DEFAULT_HOVERED_TEXT_COLOR;

    private boolean hovered = false;

    protected Button(String text, double x, double y,
                     double width, double height) {
        super(x, y);
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width and height must be non-negative");
        }
        this.width = width;
        this.height = height;
        this.text = requireNonNull(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = requireNonNull(text);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (width < 0) {
            throw new IllegalArgumentException("width must be non-negative");
        }
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height < 0) {
            throw new IllegalArgumentException("height must be non-negative");
        }
        this.height = height;
    }

    public int getFontSize() {
        return (int) (getHeight() / 2);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getHoveredBackgroundColor() {
        return hoveredBackgroundColor;
    }

    public void setHoveredBackgroundColor(Color hoveredBackgroundColor) {
        this.hoveredBackgroundColor = hoveredBackgroundColor;
    }

    public Color getHoveredTextColor() {
        return hoveredTextColor;
    }

    public void setHoveredTextColor(Color hoveredTextColor) {
        this.hoveredTextColor = hoveredTextColor;
    }

    public boolean isHovered() {
        return hovered;
    }

    public Rectangle getInteractiveArea(Gui gui) {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onMouseEnter() {
        hovered = true;
    }

    @Override
    public void onMouseExit() {
        hovered = false;
    }

    @Override
    public void onLeftClick(double x, double y) {
        // do nothing by default; subclasses can override
    }

    @Override
    public void onRightClick(double x, double y) {
        // do nothing by default; subclasses can override
    }

    @Override
    public void draw(Gui gui) {
        gui.setColor(hovered ? hoveredBackgroundColor : backgroundColor);
        gui.fillRect(getX(), getY(), getWidth(), getHeight());

        gui.setColor(hovered ? hoveredTextColor : textColor);
        gui.setFontSize(getFontSize());
        gui.setTextAlignCenter();
        gui.drawString(text, getX() + getWidth() / 2.0, getY() + getHeight() * 0.65);
    }
}
