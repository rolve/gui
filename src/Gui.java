import static java.awt.Color.WHITE;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Gui {

    public static final Gui instance = new Gui();
    
    private final JFrame frame;
    
    private Gui() {
        frame = createFrame();
        
        Thread main = Thread.currentThread();
        new Thread(() -> {
            while(true) {
                try {
                    main.join();
                    break;
                } catch (InterruptedException e) {}
            }
            invokeLater(() -> frame.dispose());
        }).start();
    }
    
    private JFrame createFrame() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        frame.setContentPane(panel);
        return frame;
    }
    
    public void open(String title, int width, int height) {
        run(() -> {
            frame.setTitle(title);
            frame.getContentPane().setSize(new Dimension(width, height));
            frame.getContentPane().setPreferredSize(new Dimension(width, height));
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    public boolean isOpen() {
        AtomicBoolean closed = new AtomicBoolean();
        run(() -> closed.set(frame.isVisible()));
        return closed.get();
    }

    public void waitUntilClosed() {
        while(isOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }
    
    private void run(Runnable run) {
        try {
            invokeAndWait(run);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        } catch (InterruptedException e) {}
    }
}