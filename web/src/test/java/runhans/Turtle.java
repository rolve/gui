package runhans;

import static runhans.RunHans.FRAME_RATE;

public class Turtle extends Enemy {

    private static final int SPEED = 50; // pixels/sec

    private double vx;

    public Turtle(double x, double y) {
        super(x, y, 30, "turtle");
    }

    @Override
    public void step(Hans hans) {
        double dx = hans.x - x;
        double dy = hans.y - y;
        // Länge des Vektors (dx, dy) berechnen, um zu
        // normalisieren:
        double length = Math.sqrt(dx*dx + dy*dy);

        vx = dx / length * SPEED / FRAME_RATE;
        double vy = dy / length * SPEED / FRAME_RATE;

        move(vx, vy);
    }

    @Override
    protected boolean isLookingLeft() {
        return vx < 0;
    }
}
