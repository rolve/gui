import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Simple {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Simple", 500, 300);
        gui.open();

        int t = 0;
        while(gui.isOpen()) {
            gui.fillRect((int) (sin(t/10.0) * 50 + 50), (int) (cos(t/10.0) * 50 + 50), 20 + t, 20 + t);
            gui.refresh(10);
            t++;
        }
    }
}
