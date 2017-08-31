import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.getKeyText;
import static java.awt.geom.AffineTransform.getRotateInstance;
import static java.awt.image.AffineTransformOp.TYPE_BICUBIC;
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
import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Gui {
    
    private final JFrame frame;
    
    private BufferedImage canvas;
    private BufferedImage snapshot;
    
    private Color color = BLACK;
    private int fontSize = 11 * getDefaultToolkit().getScreenResolution() / 96;
    
    private Map<String, BufferedImage> images = new HashMap<>();
    
    private Set<String> typedKeys = newSetFromMap(new ConcurrentHashMap<>());
    private Set<String> clearKeys = new HashSet<>();
    private Set<String> pressedKeys = newSetFromMap(new ConcurrentHashMap<>());
    
    private volatile boolean leftMouseButtonClicked = false;
    private volatile boolean rightMouseButtonClicked = false;
    private boolean clearLeftMouseButton = false;
    private boolean clearRightMouseButton = false;
    private volatile boolean leftMouseButtonPressed = false;
    private volatile boolean rightMouseButtonPressed = false;
    
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
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                    leftMouseButtonPressed = true;
                else if(SwingUtilities.isRightMouseButton(e))
                    rightMouseButtonPressed = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    leftMouseButtonPressed = false;
                    leftMouseButtonClicked = true;
                } else if(SwingUtilities.isRightMouseButton(e)) {
                    rightMouseButtonPressed = false;
                    rightMouseButtonClicked = true;
                }
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
                pressedKeys.add(keyText(e));
            }
            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(keyText(e));
                typedKeys.add(keyText(e));
            }
            private String keyText(KeyEvent e) {
                return getKeyText(e.getKeyCode()).toLowerCase();
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

        if(clearLeftMouseButton) {
            leftMouseButtonClicked = false;
            clearLeftMouseButton = false;
        }
        if(clearRightMouseButton) {
            rightMouseButtonClicked = false;
            clearRightMouseButton = false;
        }
        typedKeys.removeAll(clearKeys);
        clearKeys.clear();
        
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
    
    public void setResizable(boolean resizable) {
        run(() -> frame.setResizable(resizable));
    }
    
    /*
     * Painting
     */
    
    public void setColor(int red, int green, int blue) {
        color = new Color(clamp(red), clamp(green), clamp(blue));
    }
    
    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }
    
    public void setFontSize(int size) {
        fontSize = size;
    }
    
    public void drawRect(int x, int y, int width, int height) {
        withGraphics(g -> g.drawRect(x, y, width, height));
    }
    
    public void drawOval(int x, int y, int width, int height) {
        withGraphics(g -> g.drawOval(x, y, width, height));
    }
    
    public void drawLine(int x1, int y1, int x2, int y2) {
        withGraphics(g -> g.drawLine(x1, y1, x2, y2));
    }
    
    public void drawString(String string, int x, int y) {
        withGraphics(g -> g.drawString(string, x, y));
    }
    
    public void drawImage(String path, int x, int y) {
        withGraphics(g -> g.drawImage(getImage(path), x, y, null));
    }
    
    public void drawImage(String path, int x, int y, double angle) {
        BufferedImage image = getImage(path);
        AffineTransform rotation = getRotateInstance(angle, image.getWidth()/2, image.getHeight()/2);
        withGraphics(g -> g.drawImage(image, new AffineTransformOp(rotation, TYPE_BICUBIC), x, y));
    }
    
    private BufferedImage getImage(String path) throws Error {
        if(!images.containsKey(path)) {
            try {
                BufferedImage image = ImageIO.read(new File(path));
                if(image == null)
                    throw new Error("could not load image \"" + path + "\"");
                images.put(path, image);
            } catch (IOException e) {
                throw new Error("could not load image \"" + path + "\"", e);
            }
        }
        return images.get(path);
    }
    
    public void fillRect(int x, int y, int width, int height) {
        withGraphics(g -> g.fillRect(x, y, width, height));
    }
    
    public void fillOval(int x, int y, int width, int height) {
        withGraphics(g -> g.fillOval(x, y, width, height));
    }
    
    private void withGraphics(Consumer<Graphics2D> command) {
        Graphics2D g = canvas.createGraphics();
        g.addRenderingHints(new HashMap<Key, Object>() {{ put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON); }});
        g.setColor(color);
        g.setFont(frame.getFont().deriveFont((float) fontSize));
        command.accept(g);
        g.dispose();
    }
    
    /*
     * Input
     */
    
    public boolean isKeyPressed(String keyCode) {
        return pressedKeys.contains(keyCode.toLowerCase());
    }
    
    public boolean wasKeyTyped(String keyCode) {
        String lower = keyCode.toLowerCase();
        if(typedKeys.contains(lower))
            clearKeys.add(lower);
        return typedKeys.contains(lower);
    }
    
    public boolean isLeftMouseButtonPressed() {
        return leftMouseButtonPressed;
    }
    
    public boolean isRightMouseButtonPressed() {
        return rightMouseButtonPressed;
    }
    
    public boolean wasLeftMouseButtonClicked() {
        if(leftMouseButtonClicked)
            clearLeftMouseButton = true;
        return leftMouseButtonClicked;
    }
    
    public boolean wasRightMouseButtonClicked() {
        if(rightMouseButtonClicked)
            clearRightMouseButton = true;
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