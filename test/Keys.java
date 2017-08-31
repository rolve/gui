import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Keys {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Keys", 700, 300);
        gui.open();
        
        int speed = 5;
        int x = 100;
        int y = 100;
        int color = 0;
        while(gui.isOpen()) {
            if(gui.isKeyPressed("w") || gui.isKeyPressed("up"))
                y -= speed;
            if(gui.isKeyPressed("s") || gui.isKeyPressed("down"))
                y += speed;
            if(gui.isKeyPressed("a") || gui.isKeyPressed("left"))
                x -= speed;
            if(gui.isKeyPressed("d") || gui.isKeyPressed("right"))
                x += speed;
            if(gui.isKeyPressed("space"))
                color++;
            
            gui.setColor((int) (sin(color/2.0) * 256), (int) (cos(color/3.0) * 256), 0);
            gui.fillRect(x, y, 50, 50);
            gui.fillRect(500, 100, 50, 50);
            
            gui.refresh(20);
        }
    }
}
