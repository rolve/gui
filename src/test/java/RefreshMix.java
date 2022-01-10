import gui.Window;

public class RefreshMix {

    public static void main(String[] args) {
        Window window = new Window("Refresh Mix", 200, 200);
        window.open();
        window.refreshAndClear(1000);

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

        while (window.isOpen()) {
            window.fillCircle(50, 50, 30);
            window.refresh(1000);
            window.fillCircle(150, 50, 30);
            window.refreshAndClear(1000);
            window.fillCircle(150, 150, 30);
            window.refresh(1000);
            window.fillCircle(50, 150, 30);
            window.refreshAndClear(1000);
        }
    }
}
