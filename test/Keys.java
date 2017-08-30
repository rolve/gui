public class Keys {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Keys", 700, 300);
        gui.open();

        int speed = 1;
        int x = 100;
        int y = 100;
        while(gui.isOpen()) {
            if(gui.wasKeyPressed("w") || gui.wasKeyPressed("up"))
                y -= speed;
            if(gui.wasKeyPressed("s") || gui.wasKeyPressed("down"))
                y += speed;
            if(gui.wasKeyPressed("a") || gui.wasKeyPressed("left"))
                x -= speed;
            if(gui.wasKeyPressed("d") || gui.wasKeyPressed("right"))
                x += speed;
            
            gui.setColor(0, 0, 0);
            gui.fillRect(x, y, 50, 50);
            gui.fillRect(500, 100, 50, 50);
            
            gui.refresh(2);
        }
    }
}
