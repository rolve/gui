import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.signum;

import java.awt.MouseInfo;

public class Mouse {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Keys", 700, 300);
        gui.open();
        
        System.out.println(MouseInfo.getNumberOfButtons());
        
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
            gui.setColor(0, 0, 0);
            gui.fillRect(x - size/2, y - size/2, size + 1, size + 1);            
            gui.refresh(20);
        }
    }
}
