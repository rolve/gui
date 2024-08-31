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
    public static final Color DEFAULT_HOVER_BACKGROUND_COLOR = new Color(31, 95, 160);
    public static final Color DEFAULT_HOVER_TEXT_COLOR = DEFAULT_TEXT_COLOR;

    private String text;
    private double width;
    private double height;

    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private Color textColor = DEFAULT_TEXT_COLOR;
    private Color hoverBackgroundColor = DEFAULT_HOVER_BACKGROUND_COLOR;
    private Color hoverTextColor = DEFAULT_HOVER_TEXT_COLOR;

    private boolean hover = false;

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

    public Color getHoverBackgroundColor() {
        return hoverBackgroundColor;
    }

    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public Color getHoverTextColor() {
        return hoverTextColor;
    }

    public void setHoverTextColor(Color hoverTextColor) {
        this.hoverTextColor = hoverTextColor;
    }

    public boolean isHover() {
        return hover;
    }

    public Rectangle getInteractiveArea(Gui gui) {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onMouseEnter() {
        hover = true;
    }

    @Override
    public void onMouseExit() {
        hover = false;
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
        gui.setColor(hover ? hoverBackgroundColor : backgroundColor);
        gui.fillRect(getX(), getY(), getWidth(), getHeight());

        gui.setColor(hover ? hoverTextColor : textColor);
        gui.setFontSize((int) (getHeight() / 2));
        gui.setTextAlignCenter();
        gui.drawString(text, getX() + getWidth() / 2.0, getY() + getHeight() * 0.65);
    }
}
