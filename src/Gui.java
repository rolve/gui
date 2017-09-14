import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.getKeyText;
import static java.awt.geom.AffineTransform.getScaleInstance;
import static java.awt.image.AffineTransformOp.TYPE_BICUBIC;
import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.round;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.singletonMap;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    
    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 100;
    
    private final JFrame frame;
    private final JPanel panel;
    private final int interpolation;
    private final int pixelScale = (int) round(getDefaultToolkit().getScreenResolution() / 96.0);
    
    private BufferedImage canvas;
    private BufferedImage snapshot;
    
    private Color color = new Color(0, 0, 0);
    private int strokeWidth = 1;
    private int fontSize = 11;
    private boolean bold = false;
    
    private Map<String, BufferedImage> images = new HashMap<>();
    private Map<String, BufferedImage> scaledImages = new HashMap<>();
    
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
    private volatile int width;
    private volatile int height;
    
    private long lastRefreshTime = 0;
    
    public Gui(String title, int width, int height) {
        this(title, width, height, false);
    }
    
    public Gui(String title, int width, int height, boolean smoothInterpolation) {
        this.interpolation = smoothInterpolation ? TYPE_BICUBIC : TYPE_NEAREST_NEIGHBOR;
        this.width = width;
        this.height = height;
        
        frame = new JFrame();
        frame.setTitle(title);
        frame.setMinimumSize(new Dimension(toNative(MIN_WIDTH), toNative(MIN_HEIGHT)));
        
        panel = new JPanel() {
            public void paintComponent(Graphics g) {
                synchronized(Gui.this) {
                    g.drawImage(snapshot, 0, 0, null);
                }
            }
        };
        Dimension size = new Dimension(toNative(width), toNative(height));
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
                mouseX = toUser(e.getX());
                mouseY = toUser(e.getY());
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Gui.this.width = panel.getWidth();
                Gui.this.height = panel.getHeight();
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
            frame.setLocationRelativeTo(null); // center
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
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    /*
     * Painting
     */
    
    public void setColor(int red, int green, int blue) {
        color = new Color(red, green, blue);
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    public int getStrokeWidth() {
        return strokeWidth;
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    
    public void drawRect(int x, int y, int width, int height) {
        withGraphics(g -> g.drawRect(toNative(x), toNative(y), toNative(width), toNative(height)));
    }
    
    public void drawOval(int x, int y, int width, int height) {
        withGraphics(g -> g.drawOval(toNative(x), toNative(y), toNative(width), toNative(height)));
    }
    
    public void drawCircle(int centerX, int centerY, int radius) {
        drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    public void drawLine(int x1, int y1, int x2, int y2) {
        withGraphics(g -> g.drawLine(toNative(x1), toNative(y1), toNative(x2), toNative(y2)));
    }
    
    public void drawString(String string, int x, int y) {
        withGraphics(g -> g.drawString(string, toNative(x), toNative(y)));
    }
    
    public void drawImage(String path, int x, int y) {
        ensureLoaded(path);
        withGraphics(g -> g.drawImage(scaledImages.get(path), toNative(x), toNative(y), null));
    }
    
    public void drawImageCentered(String path, int x, int y) {
        ensureLoaded(path);
        BufferedImage img = scaledImages.get(path);
        withGraphics(g -> g.drawImage(img, toNative(x) - img.getWidth()/2, toNative(y) - img.getHeight()/2, null));
    }
    
    public void drawImage(String path, int x, int y, double scale) {
        drawImage(path, x, y, scale, 0);
    }
    
    public void drawImageCentered(String path, int x, int y, double scale) {
        drawImageCentered(path, x, y, scale, 0);
    }
    
    public void drawImage(String path, int x, int y, double scale, double angle) {
        ensureLoaded(path);
        BufferedImage image = images.get(path);
        AffineTransform transform = new AffineTransform();
        transform.scale(scale * pixelScale, scale * pixelScale);
        transform.rotate(angle, image.getWidth()/2, image.getHeight()/2);
        withGraphics(g -> g.drawImage(image, new AffineTransformOp(transform, interpolation), toNative(x), toNative(y)));
    }
    
    public void drawImageCentered(String path, int x, int y, double scale, double angle) {
        ensureLoaded(path);
        BufferedImage image = images.get(path);
        AffineTransform transform = new AffineTransform();
        transform.scale(scale * pixelScale, scale * pixelScale);
        transform.rotate(angle, image.getWidth()/2, image.getHeight()/2);
        withGraphics(g -> g.drawImage(image,
                new AffineTransformOp(transform, interpolation),
                (int) (toNative(x) - image.getWidth()/2 * pixelScale*scale),
                (int) (toNative(y) - image.getHeight()/2 * pixelScale*scale)));
    }
    
    private BufferedImage ensureLoaded(String imagePath) throws Error {
        if(!images.containsKey(imagePath)) {
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                if(image == null)
                    throw new Error("could not load image \"" + imagePath + "\"");
                images.put(imagePath, image);
                
                BufferedImage scaled;
                if(pixelScale == 1)
                    scaled = image;
                else {
                    AffineTransformOp op = new AffineTransformOp(getScaleInstance(pixelScale, pixelScale), interpolation);
                    scaled = op.filter(image, null);
                }
                scaledImages.put(imagePath, scaled);
            } catch (IOException e) {
                throw new Error("could not load image \"" + imagePath + "\"", e);
            }
        }
        return images.get(imagePath);
    }
    
    public void fill() {
        Dimension size = getDefaultToolkit().getScreenSize();
        withGraphics(g -> g.fillRect(0, 0, size.width, size.height));
    }
    
    public void fillRect(int x, int y, int width, int height) {
        withGraphics(g -> g.fillRect(toNative(x), toNative(y), toNative(width), toNative(height)));
    }
    
    public void fillOval(int x, int y, int width, int height) {
        withGraphics(g -> g.fillOval(toNative(x), toNative(y), toNative(width), toNative(height)));
    }
    
    public void fillCircle(int centerX, int centerY, int radius) {
        fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    private void withGraphics(Consumer<Graphics2D> command) {
        Graphics2D g = canvas.createGraphics();
        g.addRenderingHints(singletonMap(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        g.setColor(new java.awt.Color(color.r, color.g, color.b));
        g.setStroke(new BasicStroke(toNative(strokeWidth)));
        g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN, toNative(fontSize)));
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
    
    /** Converts the given number of "user space" pixels to native pixels (for high-DPI displays). */
    private int toNative(int pixels) {
        return pixels * pixelScale;
    }
    
    /** Converts the given number of native pixels to "user space" pixels (for high-DPI displays). */
    private int toUser(int pixels) {
        return pixels / pixelScale;
    }
}