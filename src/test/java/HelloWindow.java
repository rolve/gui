import gui.Window;

public class HelloWindow {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;

    public static void main(String[] args) {
        Window window = new Window("Hello", WIDTH, HEIGHT);

        window.setColor(240, 240, 240);
        window.fillRect(0, 0, WIDTH, HEIGHT);

        window.setFontSize(36);

        window.setColor(0, 0, 0);
        window.drawStringCentered("Hello, Window!", WIDTH / 2 + 1, HEIGHT / 2 + 2);

        window.setColor(230, 0, 0);
        window.drawStringCentered("Hello, Window!", WIDTH / 2, HEIGHT / 2);

        window.open();
        window.waitUntilClosed();
    }
}
