import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Loop {
    
    public static void main(String[] args) {
        Window window = new Window("Loop", 500, 300);
        window.open();
        
        long lastTime = System.nanoTime();
        
        int t = 0;
        while(window.isOpen()) {
            double time = t/10.0;
            double amplitude = sin(time) * 50 + 50;
            window.setColor((int) (sin(time) * 255), (int) (sin(time/PI) * 255), 10);
            window.fillRect(amplitude, cos(time) * 50 + 50, amplitude + 10, amplitude + 10);
            window.refreshAndClear(0);
            t++;
            
            System.out.println((System.nanoTime() - lastTime)/1000000);
            lastTime = System.nanoTime();
        }
    }
}
