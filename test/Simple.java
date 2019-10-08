public class Simple {

    public static void main(String[] args) {
        Window window = new Window("Simple", 500, 300);
        window.setColor(255, 0, 0);
        window.fillRect(20, 20, 50, 50);
        window.setColor(0, 255, 0);
        window.fillRect(30, 30, 50, 50);
        window.setColor(0, 0, 255);
        window.fillRect(40, 40, 50, 50);
        window.open();
        window.waitUntilClosed();
    }
}
