public class Simple {
    
    public static void main(String[] args) {
        Gui gui = new Gui("Simple", 500, 300);
        gui.fillRect(20, 20, 50, 50);
        gui.open();
        gui.waitUntilClosed();
    }
}
