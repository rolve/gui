import static java.lang.Math.atan2;

public class Bill {
    public static void main(String[] args) {
        int width = 658;
        int height = 432;
        int billSize = 64;
        
        Gui gui = new Gui("Bill", width, height);
        gui.setResizable(false);
        gui.open();
        
        double x = 100;
        double y = 100;
        double vx = 0;
        double vy = 0;
        double acc = 0.25;
        while(gui.isOpen()) {
            if(gui.isKeyPressed("w") || gui.isKeyPressed("up"))
                vy -= acc;
            if(gui.isKeyPressed("s") || gui.isKeyPressed("down"))
                vy += acc;
            if(gui.isKeyPressed("a") || gui.isKeyPressed("left"))
                vx -= acc;
            if(gui.isKeyPressed("d") || gui.isKeyPressed("right"))
                vx += acc;
            
            if(gui.wasKeyTyped("space")) {
                x = y = 100;
                vx = vy = 0;
            }

            if(x < 0 || x + billSize >= width)
                vx = -vx;
            if(y < 0 || y + billSize >= height)
                vy = -vy;
            
            x += vx;
            y += vy;
            
            gui.drawImage("background.png", 0, 0);
            gui.drawImage("bill.png", (int) x, (int) y, atan2(vy, vx));
            
            gui.refresh(20);
        }
    }
}
