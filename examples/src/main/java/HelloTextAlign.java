import gui.Gui;

public class HelloTextAlign {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;

    public static void main(String[] args) {
        Gui gui = Gui.create("Hello", WIDTH, HEIGHT);

        gui.setColor(240, 240, 240);
        gui.fillRect(0, 0, WIDTH, HEIGHT);

        gui.setFontSize(36);
        gui.setBold(true);
        gui.setTextAlignCenter();

        gui.setColor(0, 0, 0);
        gui.drawString("Hello, Gui!", WIDTH / 2.0 + 1, HEIGHT / 2.0 + 2);

        gui.setColor(230, 0, 0);
        gui.drawString("Hello, Gui!", WIDTH / 2.0, HEIGHT / 2.0);

        gui.setColor(150, 150, 150);
        gui.setFontSize(12);
        gui.setBold(false);
        gui.setTextAlignLeft();
        gui.drawString("© Awesome Corp.", 8, HEIGHT - 10);

        gui.setTextAlignRight();
        gui.drawString("2023", WIDTH - 8, HEIGHT - 10);

        gui.open();
        gui.waitUntilClosed();
    }
}
