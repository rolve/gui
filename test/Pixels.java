public class Pixels {
    public static void main(String[] args) {
        Window window = new Window("Pixels", 255, 255);
        for (int x = 0; x < 255; x++) {
            for (int y = 0; y < 255; y++) {
                window.setColor(127, x, y);
                window.fillRect(x, y, 1, 1);
            }
        }
        window.open();
        window.waitUntilClosed();
    }
}
