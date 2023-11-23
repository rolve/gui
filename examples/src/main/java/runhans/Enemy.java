package runhans;

public abstract class Enemy extends Character {

    public Enemy(double x, double y, double size, String imgName) {
        super(x, y, size, imgName);
    }

    public abstract void step(Hans hans);
}
