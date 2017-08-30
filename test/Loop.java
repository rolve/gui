import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Loop {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Simple", 500, 300);
        gui.open();

        int t = 0;
        while(gui.isOpen()) {
            double time = t/10.0;
            int amplitude = (int) (sin(time) * 50 + 50);
            gui.setColor((int) (sin(time) * 255), (int) (sin(time/PI) * 255), 10);
            gui.fillRect(amplitude, (int) (cos(time) * 50 + 50), amplitude + 10, amplitude + 10);
            gui.refresh(20);
            t++;
        }
    }
}
