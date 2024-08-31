import ch.trick17.gui.Gui;

public class MultiLineText {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;

    public static void main(String[] args) {
        Gui gui = Gui.create("Multi-line Text", WIDTH, HEIGHT);
        gui.setFontSize(16);

        gui.drawString("Welcome!\n\nHow about a\ncouple of lines\nof text?", 10, 8 + 16);

        gui.setTextAlignCenter();
        gui.setFontSize(24);
        gui.drawString("This is\ncentered", WIDTH / 2.0, 8 + 24);

        gui.setTextAlignRight();
        gui.setLineSpacing(1.5);
        gui.drawString("These\nlines\nuse\n1.5x\nspacing\n!", WIDTH - 10, 8 + 24);

        gui.setFontSize(12);
        gui.setTextAlign(-1); // left
        gui.setLineSpacing(0.8);
        gui.drawString("This is \\way\\\ntoo little spacing... (\\n works by the way)", 10, HEIGHT - 20);

        gui.open();
        gui.waitUntilClosed();
    }
}
