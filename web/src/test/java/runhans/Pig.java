package runhans;

import static runhans.RunHans.*;

public class Pig extends Enemy {

    private static final int MAX_ACC = 50;    // pixels/sec²
    private static final int MAX_SPEED = 250; // pixels/sec

    private double vx;
    private double vy;

    public Pig(double x, double y) {
        super(x, y, 30, "pig");
    }

    @Override
    public void step(Hans hans) {
        // update speed
        vx = limitSpeed(vx + randomAcceleration());
        vy = limitSpeed(vy + randomAcceleration());
        // bounce off window frame
        if (x + vx / FRAME_RATE < 0 || x + vx / FRAME_RATE > WIDTH) {
            vx = -vx;
        }
        if (y + vy / FRAME_RATE < 0 || y + vy / FRAME_RATE > HEIGHT) {
            vy = -vy;
        }
        move(vx / FRAME_RATE, vy / FRAME_RATE);
    }

    private double randomAcceleration() {
        return Math.random() * MAX_ACC * 2 - MAX_ACC;
    }

    private double limitSpeed(double speed) {
        return Math.max(-MAX_SPEED, Math.min(speed, MAX_SPEED));
    }

    @Override
    protected boolean isLookingLeft() {
        return vx < 0;
    }
}
