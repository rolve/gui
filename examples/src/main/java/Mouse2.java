import ch.trick17.gui.Gui;

public class Mouse2 {

    public static void main(String[] args) throws InterruptedException {
        Gui gui = Gui.create("Mouse 2", 700, 300);
        gui.open();

        while (gui.isOpen()) {
            // circles should be concentric; i.e. the mouse coordinates must not
            // change until refreshAndClear is called again
            for (int i = 10; i <= 100; i += 10) {
                gui.drawCircle(gui.getMouseX(), gui.getMouseY(), i);
                Thread.sleep(50);
            }
            gui.refreshAndClear(1000);
        }
    }
}
