import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import gui.Gui;

public class Loop {

    public static void main(String[] args) {
        Gui gui = Gui.create("Loop", 500, 300);
        gui.open();

        long lastTime = System.nanoTime();

        int t = 0;
        while (gui.isOpen()) {
            double time = t / 10.0;
            double amplitude = sin(time) * 50 + 50;
            gui.setColor((int) (sin(time) * 255), (int) (sin(time / PI) * 255), 10);
            gui.fillRect(amplitude, cos(time) * 50 + 50, amplitude + 10, amplitude + 10);
            gui.refreshAndClear(10);
            t++;

            System.out.println((System.nanoTime() - lastTime) / 1000000);
            lastTime = System.nanoTime();
        }
    }
}
