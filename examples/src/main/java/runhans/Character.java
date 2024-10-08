package runhans;

import ch.trick17.gui.Gui;
import ch.trick17.gui.component.Drawable;

import static runhans.RunHans.HEIGHT;
import static runhans.RunHans.WIDTH;

public abstract class Character implements Drawable {
    protected double x;
    protected double y;
    protected double size;

    private final String imgName;

    public Character(double x, double y, double size, String imgName) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.imgName = imgName;
    }

    protected void move(double dx, double dy) {
        x = Math.max(0, Math.min(x + dx, WIDTH));
        y = Math.max(0, Math.min(y + dy, HEIGHT));
    }

    public double distanceTo(Character other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double centerDistance = Math.sqrt(dx * dx + dy * dy); // Pythagoras
        return centerDistance - size - other.size;
    }

    @Override
    public void draw(Gui gui) {
        String suffix;
        if (isLookingLeft()) {
            suffix = "left";
        } else {
            suffix = "right";
        }
        String img = "img/runhans/" + imgName + "-" + suffix + ".png";
        gui.drawImageCentered(img, x, y);
    }

    protected abstract boolean isLookingLeft();
}
