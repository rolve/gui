import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
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
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
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
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A class for creating simple GUIs (graphical user interfaces). Every instance represents
 * a separate Window. The programmer can display content in the window by drawing on a canvas
 * using <code>draw...()</code> and <code>fill...()</code> methods. Note that the content of the
 * canvas is not displayed immediately, but only after a call to {@link #open()} or {@link #refresh(int)}.
 * <p>
 * There are two ways to use this class. The first way is for displaying static content.
 * First, draw the content using the various <code>draw...()</code> or <code>fill...()</code>
 * methods, then, open the window with {@link #open()}, and finally, call {@link #waitUntilClosed()}:
 * <pre>
 * Window window = new Window("Pixels", width, height);
 * window.drawString(x, y, "Hello World!");
 * window.open();
 * window.waitUntilClosed();
 * </pre>
 * The second way is for displaying dynamic and possibly interactive content. First,
 * open the window and then, draw and call {@link #refresh(int)} in a loop:
 * <pre>
 * Window window = new Window("Pixels", width, height);
 * window.open();
 * while(window.isOpen()) {
 *     window.drawString(x, y, "Hello World!");
 *     window.refresh(20);
 * }
 * </pre>
 * All methods of this class use a pixel-based coordinate system with the origin in the
 * upper-left corner of the window. The x-axis extends to the right while the y-axis extends
 * to the bottom of the window. Note that, on high-DPI monitors, one pixel in the window
 * coordinate system may correspond to multiple actual pixels on the monitor.
 */
public class Window {
    
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
    
    private Object inputLock = new Object();
    private Set<Input> pressedInputs = new HashSet<>();
    private Set<Input> releasedInputs = new HashSet<>();
    private Set<Input> pressedSnapshot = new HashSet<>();
    private Set<Input> releasedSnapshot = new HashSet<>();
    
    private volatile int mouseX = 0;
    private volatile int mouseY = 0;
    
    private volatile boolean open = false;
    private volatile int width;
    private volatile int height;
    
    private long lastRefreshTime = 0;
    
    /**
     * Create a new window with the specified title, width, and height.
     */
    public Window(String title, int width, int height) {
        this(title, width, height, false);
    }
    
    /**
     * Create a new window with the specified title, width, and height. If
     * <code>smoothInterpolation</code> is <code>true</code>, images are drawn
     * with higher quality, but at the expense of performance.
     */
    public Window(String title, int width, int height, boolean smoothInterpolation) {
        this.interpolation = smoothInterpolation ? TYPE_BICUBIC : TYPE_NEAREST_NEIGHBOR;
        this.width = width;
        this.height = height;
        
        frame = new JFrame();
        frame.setTitle(title);
        frame.setResizable(false);
        frame.setMinimumSize(new Dimension(toNative(MIN_WIDTH), toNative(MIN_HEIGHT)));
        
        panel = new JPanel() {
            public void paintComponent(Graphics g) {
                synchronized(Window.this) {
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
                synchronized(inputLock) {
                    pressedInputs.add(new MouseInput(e));
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                MouseInput input = new MouseInput(e);
                synchronized(inputLock) {
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = toUser(e.getX());
                mouseY = toUser(e.getY());
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if(x >= 0 && x < width && y >= 0 && y < height) {
                    mouseX = toUser(x);
                    mouseY = toUser(y);
                }
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Window.this.width = panel.getWidth();
                Window.this.height = panel.getHeight();
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized(inputLock) {
                    pressedInputs.add(new KeyInput(e));
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                KeyInput input = new KeyInput(e);
                synchronized(inputLock) {
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
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
    
    /**
     * Opens the window and displays the current content of the canvas.
     */
    public void open() {
        canvas.copyData(snapshot.getRaster());
        open = true;
        
        run(() -> {
            frame.pack();
            frame.setLocationRelativeTo(null); // center
            frame.setVisible(true);
        });
    }
    
    /**
     * Closes the window.
     */
    public void close() {
        run(() -> frame.setVisible(false));
    }
    
    /**
     * Returns <code>true</code> if the window is currently open, <code>false</code>
     * otherwise. Note that the window can be closed either by the programmer
     * (by calling {@link #close()}) or by the user.
     */
    public boolean isOpen() {
        return open;
    }
    
    /**
     * This method returns only once the window is closed by the user (or if it
     * was not open in the first place). More precisely, this method returns, as
     * soon as {@link #isOpen()} returns <code>true</code>.
     */
    public void waitUntilClosed() {
        while(isOpen())
            try {
                Thread.sleep((long) 50);
            } catch (InterruptedException e) {}
    }
    
    /**
     * Displays the current content of the canvas and then clears the canvas for
     * the next iteration. To achieve a constant time interval between iterations,
     * this method does not return until the given <code>waitTime</code> (in milliseconds)
     * has elapsed since the last call to {@link #refresh(int)}. For example, to
     * get a frame rate of 50 frames per second, use a <code>waitTime</code> of
     * <code>1000 / 50 = 20</code> milliseconds:
     * <pre>
     * while(window.isOpen()) {
     *     ...
     *     window.refresh(20);
     * }
     * </pre>
     * In addition, this method also clears the <code>was...Pressed()</code> and
     * <code>was...Clicked()</code> input events.
     */
    public void refresh(int waitTime) {
        synchronized(this) {
            BufferedImage newCanvas = snapshot;
            snapshot = canvas;
            canvas = newCanvas;
        }
        clear(canvas);
        
        while(true) {
            long sleepTime = (waitTime - (System.currentTimeMillis() - lastRefreshTime)) / 2;
            try {
                if(sleepTime > 1)
                    Thread.sleep(sleepTime);
                else
                	break;
            } catch (InterruptedException e) {}
        }
        lastRefreshTime = System.currentTimeMillis();
        
        frame.repaint();
        
        pressedSnapshot.clear();
        releasedSnapshot.clear();
        synchronized(inputLock) {
            pressedSnapshot.addAll(pressedInputs);
            releasedSnapshot.addAll(releasedInputs);
            releasedInputs.clear();
        }
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
    
    public void setPixel(int x, int y) {
        canvas.setRGB(x, y, color.toRgbInt());
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
        g.setStroke(new BasicStroke(toNative(strokeWidth), CAP_ROUND, JOIN_ROUND));
        g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN, toNative(fontSize)));
        command.accept(g);
        g.dispose();
    }
    
    /*
     * Input
     */
    
    public boolean isKeyPressed(String keyText) {
        return pressedSnapshot.contains(new KeyInput(keyText));
    }
    
    public boolean wasKeyTyped(String keyText) {
        return releasedSnapshot.contains(new KeyInput(keyText));
    }
    
    public boolean isLeftMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(true));
    }
    
    public boolean isRightMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(false));
    }
    
    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }
    
    public boolean wasRightMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(false));
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
    
    private static class Input {}
    
    private static class KeyInput extends Input {
        String key;
        KeyInput(KeyEvent e) {
           this(getKeyText(e.getKeyCode()));
        }
        KeyInput(String keyText) {
            this.key = keyText.toLowerCase();
        }
        public int hashCode() {
            return 31 + key.hashCode();
        }
        public boolean equals(Object obj) {
            return this == obj || obj != null && obj instanceof KeyInput && key.equals(((KeyInput) obj).key);
        }
    }
    
    private static class MouseInput extends Input {
        boolean left;
        MouseInput(MouseEvent e) {
            this(SwingUtilities.isLeftMouseButton(e));
        }
        MouseInput(boolean left) {
            this.left = left;
        }
        @Override
        public int hashCode() {
            return 31 + (left ? 1231 : 1237);
        }
        @Override
        public boolean equals(Object obj) {
            return this == obj || obj != null && obj instanceof MouseInput && left == ((MouseInput) obj).left;
        }
    }
}

class Color {
    
    public final int r, g, b;
    
    public Color(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }
    
    public int toRgbInt() {
        return r << 16 | g << 8 | b;
    }
}
