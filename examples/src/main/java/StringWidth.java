import ch.trick17.gui.Gui;

public class StringWidth {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;

    public static void main(String[] args) {
        Gui gui = Gui.create("String Width", WIDTH, HEIGHT);
        gui.setColor(100, 150, 200);
        gui.setFontSize(58);
        gui.setBold(true);
        var x = 20.0;
        var y = 90.0;
        gui.drawString("Super", x, y);

        x += gui.stringWidth("Super"); // <- this
        gui.setBold(false);
        gui.setColor(0, 0, 0);
        gui.drawString("Dry", x, y);

        gui.open();
        gui.waitUntilClosed();
    }
}
