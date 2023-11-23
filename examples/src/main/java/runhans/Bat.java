package runhans;

import static runhans.RunHans.FRAME_RATE;
import static runhans.RunHans.HEIGHT;

public class Bat extends Enemy {

    private static final int SPEED = 350; // pixels/sec

    boolean lookingLeft;
    double vy = SPEED;

    public Bat(double x, double y) {
        super(x, y, 30, "bat");
    }

    @Override
    public void step(Hans hans) {
        double newY = y + vy / FRAME_RATE;
        if (newY < 0 || newY >= HEIGHT) {
            vy = -vy;
        }
        move(0, vy / FRAME_RATE);
        lookingLeft = hans.x < x;
    }

    @Override
    protected boolean isLookingLeft() {
        return lookingLeft;
    }
}
