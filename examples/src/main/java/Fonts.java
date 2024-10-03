import ch.trick17.gui.Gui;

import java.util.List;

public class Fonts {

    public static void main(String[] args) {
        Gui gui = Gui.create("Fonts", 820, 300);
        gui.loadFont("font/fira/FiraSans-Regular.ttf");
        gui.loadFont("font/fira/FiraSans-Bold.ttf");
        gui.loadFont("font/fira/FiraSans-Italic.ttf");
        gui.loadFont("font/fira/FiraSans-BoldItalic.ttf");
        gui.loadFont("font/parisienne/Parisienne-Regular.ttf");

        int y = 50;
        gui.setFontSize(24);
        for (var family : List.of("sansserif", "serif", "monospaced", "Fira Sans", "Parisienne")) {
            gui.setFontFamily(family);

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
