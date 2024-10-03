import ch.trick17.gui.Gui;

import java.util.List;

public class Fonts {

    public static void main(String[] args) {
        Gui gui = Gui.create("Fonts", 820, 300);
        gui.setFontSize(24);

        int y = 50;

        for (var family : List.of("sansserif", "serif", "monospaced", "Garamond", "Impact")) {
            try {
                gui.setFontFamily(family);
            } catch (UnsupportedOperationException ignored) {}

            gui.setBold(false);
            gui.setItalic(false);
            gui.drawString(family, 30, y);

            gui.setBold(true);
            gui.drawString(family, 230, y);

            gui.setBold(false);
            gui.setItalic(true);
            gui.drawString(family, 430, y);

            gui.setBold(true);
            gui.drawString(family, 630, y);

            y += 50;
        }

        gui.open();
        gui.waitUntilClosed();
    }
}
