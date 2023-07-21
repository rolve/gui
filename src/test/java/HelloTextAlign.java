import gui.Window;

public class HelloTextAlign {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;

    public static void main(String[] args) {
        Window window = new Window("Hello", WIDTH, HEIGHT);

        window.setColor(240, 240, 240);
        window.fillRect(0, 0, WIDTH, HEIGHT);

        window.setFontSize(36);
        window.setTextAlignCenter();

        window.setColor(0, 0, 0);
        window.drawString("Hello, Window!", WIDTH / 2.0 + 1, HEIGHT / 2.0 + 2);

        window.setColor(230, 0, 0);
        window.drawString("Hello, Window!", WIDTH / 2.0, HEIGHT / 2.0);

        window.setFontSize(12);
        window.setColor(150, 150, 150);
        window.setTextAlignLeft();
        window.drawString("© Awesome Corp.", 8, HEIGHT - 10);

        window.setTextAlignRight();
        window.drawString("2023", WIDTH - 8, HEIGHT - 10);

        window.open();
        window.waitUntilClosed();
    }
}
