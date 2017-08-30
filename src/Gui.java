import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Gui {
    
    private final JFrame frame;
    
    private BufferedImage canvas;
    private BufferedImage snapshot;
    
    private long lastRefreshTime = 0;
    
    public Gui(String title, int width, int height) {
        frame = createFrame();

        Dimension size = new Dimension(width, height);
        canvas = newCanvas();
        snapshot = newCanvas();
        run(() -> {
            frame.setTitle(title);
            frame.setMinimumSize(new Dimension(200, 100));
            frame.getContentPane().setSize(size);
            frame.getContentPane().setPreferredSize(size);
        });
        
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
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(snapshot, 0, 0, null);
            }
        };
        panel.setBackground(WHITE);
        frame.setContentPane(panel);
        return frame;
    }
    
    public void open() {
        run(() -> {
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    public void close() {
        run(() -> frame.setVisible(false));
    }
    
    public boolean isOpen() {
        AtomicBoolean closed = new AtomicBoolean();
        run(() -> closed.set(frame.isVisible()));
        return closed.get();
    }
    
    public void waitUntilClosed() {
        while(isOpen())
            try {
                Thread.sleep((long) 50);
            } catch (InterruptedException e) {}
    }
    
    public void refresh(int waitTime) {
        BufferedImage newCanvas = snapshot;
        snapshot = canvas;
        canvas = newCanvas;
        clear(canvas);
        
        while(true) {
            long sleepTime = (waitTime - (System.currentTimeMillis() - lastRefreshTime)) / 2;
            try {
                if(sleepTime > 1)
                    Thread.sleep(sleepTime);
                break;
            } catch (InterruptedException e) {}
        }
        lastRefreshTime = System.currentTimeMillis();
        
        frame.repaint();
    }
    
    private static void clear(BufferedImage image) {
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for(int i = 0; i < data.length; i++)
            data[i] = 0xFFFFFFFF;
    }
    
    private BufferedImage newCanvas() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage canvas = new BufferedImage(size.width, size.height, TYPE_INT_RGB);
        canvas.getGraphics().setColor(WHITE);
        canvas.getGraphics().fillRect(0, 0, size.width, size.height);
        return canvas;
    }
    
    public void fillRect(int x, int y, int width, int height) {
        Graphics graphics = canvas.getGraphics();
        graphics.setColor(BLACK);
        graphics.fillRect(x, y, width, height);
    }
    
    private void run(Runnable run) {
        try {
            invokeAndWait(run);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        } catch (InterruptedException e) {}
    }
}