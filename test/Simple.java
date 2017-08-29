public class Simple {
    
    public static void main(String[] args) throws InterruptedException {
        Gui gui = Gui.instance;
        gui.open("Simple", 500, 300);
        gui.waitUntilClosed();
    }
}
