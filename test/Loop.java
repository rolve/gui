import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Loop {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Simple", 500, 300);
        gui.open();

        int t = 0;
        while(gui.isOpen()) {
            int amplitude = (int) (sin(t/10.0) * 50 + 50);
            gui.fillRect(amplitude, (int) (cos(t/10.0) * 50 + 50), amplitude + 10, amplitude + 10);
            gui.refresh(10);
            t++;
        }
    }
}
