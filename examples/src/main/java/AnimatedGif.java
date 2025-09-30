import ch.trick17.gui.Gui;

public class AnimatedGif {

    public static void main(String[] args) {
        var gui = Gui.create("Animated GIF", 498, 498);
        gui.open();
        while (gui.isOpen()) {
            gui.drawImage("img/cat-space.gif", 0, 0);
            gui.refreshAndClear(20);
        }
    }
}
