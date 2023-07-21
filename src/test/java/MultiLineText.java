import gui.Window;

public class MultiLineText {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 200;

    public static void main(String[] args) {
        Window window = new Window("Multi-line Text", WIDTH, HEIGHT);
        window.setFontSize(16);

        window.drawString("Welcome!\n\nHow about a\ncouple of lines\nof text?", 10, 8 + 16);

        window.setTextAlignCenter();
        window.setFontSize(24);
        window.drawString("This is\ncentered", WIDTH / 2.0, 8 + 24);

        window.setTextAlignRight();
        window.setFontSize(12);
        window.drawString("So\nmany\nlines,\nit's\nquite\nodd\n!", WIDTH - 10, 8 + 12);

        window.open();
        window.waitUntilClosed();
    }
}
