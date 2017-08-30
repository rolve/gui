import static java.awt.Color.WHITE;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.getKeyText;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.newSetFromMap;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Gui {
    
    private final JFrame frame;
    
    private BufferedImage canvas;
    private BufferedImage snapshot;
    
    private Graphics2D g;

    private Set<String> pressedKeys = newSetFromMap(new ConcurrentHashMap<>());
    private volatile boolean leftMouseButtonClicked = false;
    private volatile boolean rightMouseButtonClicked = false;
    private boolean clearMouseButtons = false;
    private volatile int mouseX = 0;
    private volatile int mouseY = 0;
    private volatile boolean open = false;
    
    private long lastRefreshTime = 0;
    
    public Gui(String title, int width, int height) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setMinimumSize(new Dimension(200, 100));
        
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                synchronized(Gui.this) {
                    g.drawImage(snapshot, 0, 0, null);
                }
            }
        };
        Dimension size = new Dimension(width, height);
        panel.setSize(size);
        panel.setPreferredSize(size);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                    leftMouseButtonClicked = true;
                if(SwingUtilities.isRightMouseButton(e))
                    rightMouseButtonClicked = true;
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(getKeyText(e.getKeyCode()).toLowerCase());
            }
            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(getKeyText(e.getKeyCode()).toLowerCase());
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                open = false;
            }
        });
        frame.setContentPane(panel);

        canvas = newCanvas();
        snapshot = newCanvas();
        g = canvas.createGraphics();
        
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
    
    public void open() {
        canvas.copyData(snapshot.getRaster());
        open = true;
        
        run(() -> {
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    public void close() {
        run(() -> frame.setVisible(false));
    }
    
    public boolean isOpen() {
        return open;
    }
    
    public void waitUntilClosed() {
        while(isOpen())
            try {
                Thread.sleep((long) 50);
            } catch (InterruptedException e) {}
    }
    
    public void refresh(int waitTime) {
        synchronized(this) {
            BufferedImage newCanvas = snapshot;
            snapshot = canvas;
            canvas = newCanvas;
        }
        clear(canvas);
        
        g.dispose();
        g = canvas.createGraphics();
        
        if(clearMouseButtons) {
            leftMouseButtonClicked = false;
            rightMouseButtonClicked = false;
            clearMouseButtons = false;
        }
        
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
        Dimension size = getDefaultToolkit().getScreenSize();
        BufferedImage canvas = new BufferedImage(size.width, size.height, TYPE_INT_RGB);
        Graphics g = canvas.getGraphics();
        g.setColor(WHITE);
        g.fillRect(0, 0, size.width, size.height);
        g.dispose();
        return canvas;
    }
    
    /*
     * Painting
     */
    
    public void setColor(int red, int green, int blue) {
        g.setColor(new Color(clamp(red), clamp(green), clamp(blue)));
    }
    
    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }
    
    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }
    
    /*
     * Input
     */
    
    public boolean isKeyPressed(String keyCode) {
        return pressedKeys.contains(keyCode.toLowerCase());
    }
    
    public boolean wasLeftMouseButtonClicked() {
        if(leftMouseButtonClicked)
            clearMouseButtons = true;
        return leftMouseButtonClicked;
    }
    
    public boolean wasRightMouseButtonClicked() {
        if(rightMouseButtonClicked)
            clearMouseButtons = true;
        return rightMouseButtonClicked;
    }
    
    public int getMouseX() {
        return mouseX;
    }
    
    public int getMouseY() {
        return mouseY;
    }
    
    private void run(Runnable run) {
        try {
            invokeAndWait(run);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        } catch (InterruptedException e) {}
    }
}