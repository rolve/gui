package runhans;

import static runhans.RunHans.FRAME_RATE;
import static runhans.RunHans.WIDTH;

public class Rhino extends Enemy {

    private static final int SPEED = 350; // pixels/sec

    double vx = SPEED;

    public Rhino(double x, double y) {
        super(x, y, 35, "rhino");
    }

    @Override
    public void step(Hans hans) {
        double newY = x + vx / FRAME_RATE;
        if (newY < 0 || newY >= WIDTH) {
            vx = -vx;
        }
        move(vx / FRAME_RATE, 0);
    }

    @Override
    protected boolean isLookingLeft() {
        return vx < 0;
    }
}
