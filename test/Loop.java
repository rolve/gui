import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Loop {
    
    public static void main(String[] args) {
        Window window = new Window("Loop", 500, 300);
        window.open();
        
        int t = 0;
        while(window.isOpen()) {
            double time = t/10.0;
            int amplitude = (int) (sin(time) * 50 + 50);
            window.setColor((int) (sin(time) * 255), (int) (sin(time/PI) * 255), 10);
            window.fillRect(amplitude, (int) (cos(time) * 50 + 50), amplitude + 10, amplitude + 10);
            window.refresh(20);
            t++;
        }
    }
}
