import ch.trick17.gui.web.WebGui;

import java.util.Random;

public class Car {
    Random random = new Random();
    String name;
    double speed;
    double x;
    double y;

    public Car(String name, double speed, double x, double y) {
        this.name = name;
        this.speed = speed;
        this.x = x;
        this.y = y;
    }

    public void drive() {
        x += speed;
        speed += random.nextDouble() * 0.2 - 0.1;
    }

    public void draw(WebGui gui) {
        gui.drawImageCentered("img/" + name + ".png", x, y);
    }
}
