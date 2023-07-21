import gui.Window;

public class MultiLineText {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;

    public static void main(String[] args) {
        Window window = new Window("Multi-line Text", WIDTH, HEIGHT);
        window.setFontSize(16);

        window.drawString("Welcome!\n\nHow about a\ncouple of lines\nof text?", 10, 8 + 16);

        window.setTextAlignCenter();
        window.setFontSize(24);
        window.drawString("This is\ncentered", WIDTH / 2.0, 8 + 24);

        window.setTextAlignRight();
        window.setLineSpacing(1.5);
        window.drawString("These\nlines\nuse\n1.5x\nspacing\n!", WIDTH - 10, 8 + 24);

        window.setFontSize(12);
        window.setTextAlign(-1); // left
        window.setLineSpacing(0.8);
        window.drawString("This is way\ntoo little spacing...", 10, HEIGHT - 20);

        window.open();
        window.waitUntilClosed();
    }
}
