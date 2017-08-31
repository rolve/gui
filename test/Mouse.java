import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.signum;

import java.awt.Toolkit;

public class Mouse {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Keys", 700, 300);
        gui.setColor(0, 128, 0);
        gui.open();
        
        System.out.println(Toolkit.getDefaultToolkit().getScreenResolution());
        
        int x = 50;
        int y = 50;
        int size = 16;
        while(gui.isOpen()) {
            if(gui.wasLeftMouseButtonClicked())
                size *= 2;
            if(gui.wasRightMouseButtonClicked())
                size = max(size / 2, 1);
            
            int xDiff = x - gui.getMouseX();
            int yDiff = y - gui.getMouseY();
            x -= signum(xDiff) * ceil(abs(-xDiff / (double) size));
            y -= signum(yDiff) * ceil(abs(-yDiff / (double) size));
            gui.drawLine(gui.getMouseX(), gui.getMouseY(), x, y);
            gui.fillOval(x - size/2, y - size/2, size + 1, size + 1);
            gui.drawString(x + ", " + y, x, y - size/2 - size/10 - 2);
            gui.refresh(20);
        }
    }
}
