package runhans;

import ch.trick17.gui.web.WebGui;

import static runhans.RunHans.FRAME_RATE;

public class Hans extends Character {

    private static final double SPEED = 300; // pixels/sec

    private boolean lookingLeft = true;

    public Hans(double x, double y) {
        super(x, y, 20, "hans");
    }

    public void step(WebGui window) {
        if (window.isKeyPressed("LEFT")) {
            move(-SPEED / FRAME_RATE, 0);
            lookingLeft = true;
        }
        if (window.isKeyPressed("RIGHT")) {
            move(SPEED / FRAME_RATE, 0);
            lookingLeft = false;
        }
        if (window.isKeyPressed("UP")) {
            move(0, -SPEED / FRAME_RATE);
        }
        if (window.isKeyPressed("DOWN")) {
            move(0, SPEED / FRAME_RATE);
        }
    }

    @Override
    protected boolean isLookingLeft() {
        return lookingLeft;
    }
}
