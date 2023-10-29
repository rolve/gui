import ch.trick17.gui.web.WebGui;

public class CarRace {

    public static void main(String[] args) {
        int width = 1258;
        int height = 600;
        int finishLine = width - 50;

        var gui = new WebGui("Cars", width, height);
        gui.drawImage("web/img/city.jpg", 0, 0);
        gui.open();

        Car[] cars = {
                new Car("Bug", 2.8, 50, height/2 - 70),
                new Car("CopCar", 4.1, 50, height/2 - 35),
                new Car("GT-A1", 4.2, 50, height/2),
                new Car("HotDogVan", 3.7, 50, height/2 + 40),
                new Car("Tank", 1.8, 50, height/2 + 90)};

        while (gui.isOpen()) {
            gui.drawImage("web/img/city.jpg", 0, 0);

            for (Car car : cars) {
                if (car.x < finishLine) {
                    car.drive();
                }
                car.draw(gui);
            }

            gui.refreshAndClear(20);
        }
        gui.waitUntilClosed();
    }
}
