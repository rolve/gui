public class Simple {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Simple", 500, 300);
        gui.setColor(255, 0, 0);
        gui.fillRect(20, 20, 50, 50);
        gui.setColor(0, 255, 0);
        gui.fillRect(30, 30, 50, 50);
        gui.setColor(0, 0, 255);
        gui.fillRect(40, 40, 50, 50);
        gui.open();
        gui.waitUntilClosed();
    }
}
