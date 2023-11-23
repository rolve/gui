import static java.lang.Math.max;

import gui.Color;
import gui.Gui;
import gui.component.Clickable;
import gui.component.Drawable;
import gui.component.Hoverable;
import gui.component.Rectangle;

public class Components {

    public static void main(String[] args) {
        Gui gui = Gui.create("Components", 700, 300);
        gui.setResizable(true);

        gui.addComponent(new GreenCircle(220, 60));
        gui.addComponent(new ClickySquare(50, 50, 50));
        gui.addComponent(new ClickySquare(200, 50, 70));
        gui.addComponent(new Greeting(300, 100));

        gui.open();
        gui.runUntilClosed(20);
    }
}

class GreenCircle implements Drawable {

    private double x;
    private double y;

    public GreenCircle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Gui gui) {
        gui.setColor(0, 180, 0);
        gui.fillCircle(x, y, 30);
    }
}

class Greeting implements Drawable, Hoverable {

    private String text = "Hello";
    private double x;
    private double y;

    public Greeting(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Rectangle getInteractiveArea(Gui gui) {
        return new Rectangle(x, y, 70, 30);
    }

    @Override
    public void onMouseEnter() {
        text = "World!";
        x += 10;
    }

    @Override
    public void onMouseExit() {
        text = "Hello";
        x -= 5;
    }

    @Override
    public void draw(Gui gui) {
        gui.setColor(240, 240, 240);
        gui.fillRect(x, y, 70, 30);
        gui.setColor(0, 0, 0);
        gui.setFontSize(16);
        gui.setBold(text.equals("World!"));
        gui.drawString(text, x + 5, y + 20);
    }
}

class ClickySquare implements Drawable, Hoverable, Clickable {

    private double x;
    private double y;
    private double size;

    private Color color = new Color(0, 0, 0);

    public ClickySquare(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    @Override
    public void draw(Gui gui) {
        gui.setColor(color);
        gui.fillRect(x, y, size, size);
    }

    @Override
    public Rectangle getInteractiveArea(Gui gui) {
        return new Rectangle(x, y, size, size);
    }

    @Override
    public void onMouseEnter() {
        color = new Color(220, 0, 0);
    }

    @Override
    public void onMouseExit() {
        color = new Color(0, 0, 0);
    }

    @Override
    public void onLeftClick(double x, double y) {
        size += 10;
    }

    @Override
    public void onRightClick(double x, double y) {
        size = max(size - 10, 10);
    }
}