import gui.Gui;

public class RefreshMix {

    public static void main(String[] args) {
        Gui gui = Gui.create("Refresh Mix", 200, 200);
        gui.open();
        gui.refreshAndClear(1000);

        // expected result:
        // .
        //
        // ---
        // . .
        //
        // ---
        //
        //   .
        // ---
        //
        // . .

        while (gui.isOpen()) {
            gui.fillCircle(50, 50, 30);
            gui.refresh(1000);
            gui.fillCircle(150, 50, 30);
            gui.refreshAndClear(1000);
            gui.fillCircle(150, 150, 30);
            gui.refresh(1000);
            gui.fillCircle(50, 150, 30);
            gui.refreshAndClear(1000);
        }
    }
}
