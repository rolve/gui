package runhans;

import ch.trick17.gui.Gui;

import static runhans.RunHans.FRAME_RATE;

public class Hans extends Character {

    private static final double SPEED = 300; // pixels/sec

    private boolean lookingLeft = true;

    public Hans(double x, double y) {
        super(x, y, 20, "hans");
    }

    public void step(Gui gui) {
        if (gui.isKeyPressed("LEFT")) {
            move(-SPEED / FRAME_RATE, 0);
            lookingLeft = true;
        }
        if (gui.isKeyPressed("RIGHT")) {
            move(SPEED / FRAME_RATE, 0);
            lookingLeft = false;
        }
        if (gui.isKeyPressed("UP")) {
            move(0, -SPEED / FRAME_RATE);
        }
        if (gui.isKeyPressed("DOWN")) {
            move(0, SPEED / FRAME_RATE);
        }
    }

    @Override
    protected boolean isLookingLeft() {
        return lookingLeft;
    }
}
