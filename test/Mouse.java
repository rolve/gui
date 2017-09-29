import static java.lang.Math.max;

public class Mouse {
    
    public static void main(String[] args) {
        Window window = new Window("Keys", 700, 300);
        window.setResizable(true);
        window.setColor(0, 128, 0);
        window.open();
        
        double x = 50;
        double y = 50;
        int size = 16;
        while(window.isOpen()) {
            if(window.wasLeftMouseButtonClicked())
                size *= 2;
            if(window.isRightMouseButtonPressed())
                size = max(size / 2, 1);
            
            double xDiff = x - window.getMouseX();
            double yDiff = y - window.getMouseY();
            x -= xDiff / size;
            y -= yDiff / size;
            window.drawLine(window.getMouseX(), window.getMouseY(), x, y);
            window.fillOval(x - size/2, y - size/2, size + 1, size + 1);
            window.drawString((int) x + ", " + (int) y, x, y - size/2 - size/10 - 2);
            window.refresh(20);
        }
    }
}
