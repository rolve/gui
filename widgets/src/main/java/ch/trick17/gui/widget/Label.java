package ch.trick17.gui.widget;

import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;

import static java.util.Objects.requireNonNull;

public class Label extends Widget {

    public static final Color DEFAULT_TEXT_COLOR = new Color(0, 0, 0);

    private String text;
    private double height;
    private Color textColor = DEFAULT_TEXT_COLOR;

    public Label(String text, double x, double y, double height) {
        super(x, y);
        if (height < 0) {
            throw new IllegalArgumentException("height must be non-negative");
        }
        this.text = requireNonNull(text);
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = requireNonNull(text);
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

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    @Override
    public void draw(Gui gui) {
        gui.setColor(textColor);
        gui.setFontSize((int) (getHeight() / 2));
        gui.drawString(text, getX(), getY() + height * 0.65);
    }
}
