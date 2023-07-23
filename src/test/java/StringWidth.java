import gui.Window;

public class StringWidth {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;

    public static void main(String[] args) {
        Window window = new Window("String Width", WIDTH, HEIGHT);
        window.setColor(100, 150, 200);
        window.setFontSize(58);
        window.setBold(true);
        var x = 20.0;
        var y = 90.0;
        window.drawString("Super", x, y);

        x += window.stringWidth("Super"); // <- this
        window.setBold(false);
        window.setColor(0, 0, 0);
        window.drawString("Dry", x, y);

        window.open();
        window.waitUntilClosed();
    }
}
