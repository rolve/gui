import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.signum;

public class Mouse {
    
    public static void main(String[] args) {
        Window window = new Window("Keys", 700, 300);
        window.setColor(0, 128, 0);
        window.open();
        
        int x = 50;
        int y = 50;
        int size = 16;
        while(window.isOpen()) {
            if(window.wasLeftMouseButtonClicked())
                size *= 2;
            if(window.isRightMouseButtonPressed())
                size = max(size / 2, 1);
            
            int xDiff = x - window.getMouseX();
            int yDiff = y - window.getMouseY();
            x -= signum(xDiff) * ceil(abs(-xDiff / (double) size));
            y -= signum(yDiff) * ceil(abs(-yDiff / (double) size));
            window.drawLine(window.getMouseX(), window.getMouseY(), x, y);
            window.fillOval(x - size/2, y - size/2, size + 1, size + 1);
            window.drawString(x + ", " + y, x, y - size/2 - size/10 - 2);
            window.refresh(20);
        }
    }
}
