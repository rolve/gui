package runhans;

public class Plant extends Enemy {

    private boolean lookingLeft;

    public Plant(double x, double y) {
        super(x, y, 30, "plant");
    }

    @Override
    public void step(Hans hans) {
        lookingLeft = hans.x < x;
    }

    @Override
    protected boolean isLookingLeft() {
        return lookingLeft;
    }
}
