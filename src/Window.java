import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.geom.AffineTransform.getScaleInstance;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    
    private static final Set<String> legalKeyTexts = new HashSet<>();
    private static final Map<Integer, String> code2text = new HashMap<>();
    
    static {
        for(Field field : KeyEvent.class.getFields()) {
            String name = field.getName();
            if(name.startsWith("VK_")) {
                String text = name.substring(3).toLowerCase();
                try {
                    int code = field.getInt(KeyEvent.class);
                    legalKeyTexts.add(text);
                    code2text.put(code, text);
                } catch(Exception e) {}
            }
        }
    }
    
    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 100;
    
    private final JFrame frame;
    private final JPanel panel;
    private final int pixelScale = (int) round(getDefaultToolkit().getScreenResolution() / 96.0);
    
    private BufferedImage canvas;
    private BufferedImage snapshot;
    
    private Color color = new Color(0, 0, 0);
    private double strokeWidth = 1;
    private boolean roundStroke = false;
    private int fontSize = 11;
    private boolean bold = false;
    
    private Map<String, BufferedImage> images = new HashMap<>();
    private Map<String, BufferedImage> scaledImages = new HashMap<>();
    
    private Object inputLock = new Object();
    private Set<Input> pressedInputs = new HashSet<>();
    private Set<Input> releasedInputs = new HashSet<>();
    private Set<Input> pressedSnapshot = new HashSet<>();
    private Set<Input> releasedSnapshot = new HashSet<>();
    
    private volatile double mouseX = 0;
    private volatile double mouseY = 0;
    
    private volatile boolean open = false;
    private volatile double width;
    private volatile double height;
    
    private long lastRefreshTime = 0;
   
    /**
     * Create a new window with the specified title, width, and height.
     */
    public Window(String title, int width, int height) {
        this.width = width;
        this.height = height;
        
        frame = new JFrame();
        frame.setTitle(title);
        frame.setResizable(false);
        frame.setMinimumSize(new Dimension((int) toNative(MIN_WIDTH), (int) toNative(MIN_HEIGHT)));
        
        panel = new JPanel() {
            public void paintComponent(Graphics g) {
                synchronized(Window.this) {
                    g.drawImage(snapshot, 0, 0, null);
                }
            }
        };
        Dimension size = new Dimension((int) toNative(width), (int) toNative(height));
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
                Window.this.width = toUser(panel.getWidth());
                Window.this.height = toUser(panel.getHeight());
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
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.setAlwaysOnTop(false);
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
     * Displays the current content of the canvas. Use this method in a loop,
     * together with {@link #isOpen()}:
     * 
     * <pre>
     * while(window.isOpen()) {
     *     ...
     *     window.refresh();
     * }
     * </pre>
     * 
     * In addition, this method also clears the <code>was...Pressed()</code> and
     * <code>was...Clicked()</code> input events.
     * <p>
     * Note that this method is equivalent to {@link #refresh(int) refresh(0)}.
     * 
     * @see #refreshAndClear()
     */
    public void refresh() {
        refresh(0);
    }
    
    /**
     * Displays the current content of the canvas. To achieve a constant time
     * interval between iterations, this method does not return until the given
     * <code>waitTime</code> (in milliseconds) has elapsed since the last refresh.
     * For example, to get a frame rate of 50 frames per second, use a
     * <code>waitTime</code> of <code>1000 / 50 = 20</code> milliseconds:
     * <pre>
     * while(window.isOpen()) {
     *     ...
     *     window.refresh(20);
     * }
     * </pre>
     * 
     * In addition, this method also clears the <code>was...Pressed()</code> and
     * <code>was...Clicked()</code> input events.
     * 
     * @see #refreshAndClear(int)
     */
    public void refresh(int waitTime) {
        refresh(waitTime, false);
    }
    
    /**
     * Displays the current content of the canvas, clears the
     * <code>was...Pressed()</code> and <code>was...Clicked()</code> input events,
     * and then clears the canvas for the next iteration. Call this method instead
     * of {@link #refresh()} if every frame is drawn from scratch.
     * <p>
     * Note that this method is equivalent to {@link #refreshAndClear(int)
     * refreshAndClear(0)}.
     */
    public void refreshAndClear() {
        refreshAndClear(0);
    }
    
    /**
     * Displays the current content of the canvas, clears the
     * <code>was...Pressed()</code> and <code>was...Clicked()</code> input events,
     * and then clears the canvas for the next iteration. Call this method instead
     * of {@link #refresh(int)} if every frame is drawn from scratch. To achieve a
     * constant time interval between iterations, this method does not return until
     * the given <code>waitTime</code> (in milliseconds) has elapsed since the last
     * refresh.
     */
    public void refreshAndClear(int waitTime) {
        refresh(waitTime, true);
    }
    
    private void refresh(int waitTime, boolean clear) {
        if(clear) {
            synchronized(this) {
                BufferedImage newCanvas = snapshot;
                snapshot = canvas;
                canvas = newCanvas;
            }
            clear(canvas);
        } else {
            synchronized(this) {
                Graphics g = snapshot.getGraphics();
                g.drawImage(canvas, 0, 0, null);
                g.dispose();
            }
        }
        
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
    
    /**
     * If <code>resizable</code> is <code>true</code>, this window can be
     * resized by the user. By default, windows are non-resizable.
     * <p>
     * For resizable windows, use {@link #getWidth()} and {@link #getHeight()}
     * to get the current window size.
     */
    public void setResizable(boolean resizable) {
        run(() -> frame.setResizable(resizable));
    }
    
    /**
     * Returns the current window width.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Returns the current window height (excluding any title bar added
     * by the operating system).
     */
    public double getHeight() {
        return height;
    }
    
    /*
     * Painting
     */
    
    /**
     * Sets the color for the subsequent drawing operations. The three
     * parameters represent the red, green, and blue channel and are
     * expected to be in the 0&ndash;255 range. Values outside this
     * range will be clamped. The default color is black (0, 0, 0).
     */
    public void setColor(int red, int green, int blue) {
        color = new Color(red, green, blue);
    }
    
    /**
     * Sets the color for the subsequent drawing operations, using a
     * {@link Color} object. The default color is black (0, 0, 0).
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Returns the current drawing color.
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the stroke width for subsequent <code>draw...()</code>
     * operations, in pixels. The default stroke width is 1 pixel.
     */
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    /**
     * Returns the current stroke width (in pixels).
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }
    
    /**
     * If <code>roundStroke</code> is <code>true</code>, subsequent
     * <code>draw...()</code> operations will use round stroke caps
     * and joins instead of flat caps and miter joins.
     */
    public void setRoundStroke(boolean roundStroke) {
        this.roundStroke = roundStroke;
    }
    
    /**
     * Sets the font size for subsequent {@link #drawString(String, int, int)}
     * operations, in points. The default font size is 11 points.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    /**
     * Returns the current font size, in points.
     */
    public int getFontSize() {
        return fontSize;
    }
    
    /**
     * If <code>bold</code> is <code>true</code>, subsequent
     * {@link #drawString(String, int, int)} operations will use a bold
     * font.
     */
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    
    /**
     * Draws the outline of a rectangle with the upper-left corner at
     * (<code>x</code>, <code>y</code>) and the given <code>width</code>
     * and <code>height</code>. The current {@linkplain #getColor() color}
     * and {@linkplain #getStrokeWidth() stroke width} are used.
     */
    public void drawRect(double x, double y, double width, double height) {
        withGraphics(g -> g.draw(new Rectangle2D.Double(toNative(x), toNative(y), toNative(width), toNative(height))));
    }
    
    /**
     * Draws the outline of an oval with a rectangular bounding box that has
     * the upper-left corner at (<code>x</code>, <code>y</code>) and the given
     * <code>width</code> and <code>height</code>. The current
     * {@linkplain #getColor() color} and {@linkplain #getStrokeWidth() stroke width}
     * are used.
     */
    public void drawOval(double x, double y, double width, double height) {
        withGraphics(g -> g.draw(new Ellipse2D.Double(toNative(x), toNative(y), toNative(width), toNative(height))));
    }
    
    /**
     * Draws the outline of a circle with the center at (<code>x</code>, <code>y</code>)
     * and the given <code>radius</code>. The current {@linkplain #getColor() color}
     * and {@linkplain #getStrokeWidth() stroke width} are used.
     */
    public void drawCircle(double centerX, double centerY, double radius) {
        drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    /**
     * Draws a line from (<code>x1</code>, <code>y1</code>) to (<code>x2</code>, <code>y2</code>).
     * The current {@linkplain #getColor() color} and {@linkplain #getStrokeWidth() stroke width}
     * are used.
     */
    public void drawLine(double x1, double y1, double x2, double y2) {
        withGraphics(g -> g.draw(new Line2D.Double(toNative(x1), toNative(y1), toNative(x2), toNative(y2))));
    }
    
    /**
     * Draw the given string with the current {@linkplain #getColor() color}.
     * The baseline of the first character is at position (<code>x</code>, <code>y</code>).
     */
    public void drawString(String string, double x, double y) {
        withGraphics(g -> g.drawString(string, (float) toNative(x), (float) toNative(y)));
    }
    
    /**
     * Draws the image found at the given <code>path</code> with the upper-left
     * corner at position (<code>x</code>, <code>y</code>).
     * <p>
     * For homework submissions, put all images in the project directory and refer
     * to them using relative paths (i.e., not starting with "C:\" or "/"). For
     * example, an image called "image.jpg" in the project folder can be referred to
     * simply using the path "image.jpg". If you put the image into a subfolder,
     * e.g., "images", refer to it using the path "images/image.jpg". Also, make
     * sure to commit all required images to the SVN repository.
     */
    public void drawImage(String path, double x, double y) {
        ensureLoaded(path);
        withGraphics(g -> g.drawImage(scaledImages.get(path), getTranslateInstance(toNative(x), toNative(y)), null));
    }
    
    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y) {
        ensureLoaded(path);
        BufferedImage img = scaledImages.get(path);
        withGraphics(g -> g.drawImage(img, getTranslateInstance(toNative(x) - img.getWidth()/2, toNative(y) - img.getHeight()/2), null));
    }
    
    /**
     * Draws the image found at the given <code>path</code> with the upper-left
     * corner at position (<code>x</code>, <code>y</code>) and scales it by the
     * given <code>scale</code>. For example, a scale of 2.0 doubles the size of the
     * image.
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImage(String path, double x, double y, double scale) {
        drawImage(path, x, y, scale, 0);
    }
    
    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>) and scales it by the given <code>scale</code>.
     * For example, a scale of 2.0 doubles the size of the image.
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y, double scale) {
        drawImageCentered(path, x, y, scale, 0);
    }
    
    private void drawImage(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        BufferedImage image = images.get(path);
        AffineTransform transform = new AffineTransform();
        transform.translate(toNative(x), toNative(y));
        transform.scale(scale * pixelScale, scale * pixelScale);
        transform.rotate(angle, image.getWidth()/2, image.getHeight()/2);
        withGraphics(g -> g.drawImage(image, transform, null));
    }
    
    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>), scales it by the given <code>scale</code>
     * and rotates it by the given <code>angle</code>, in radians (0&ndash;2&times;{@linkplain Math#PI &pi;}).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        BufferedImage image = images.get(path);
        AffineTransform transform = new AffineTransform();
        transform.translate(toNative(x) - image.getWidth()/2 * pixelScale*scale,
                toNative(y) - image.getHeight()/2 * pixelScale*scale);
        transform.scale(scale * pixelScale, scale * pixelScale);
        transform.rotate(angle, image.getWidth()/2, image.getHeight()/2);
        withGraphics(g -> g.drawImage(image, transform, null));
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
                    AffineTransformOp op = new AffineTransformOp(getScaleInstance(pixelScale, pixelScale), TYPE_NEAREST_NEIGHBOR);
                    scaled = op.filter(image, null);
                }
                scaledImages.put(imagePath, scaled);
            } catch (IOException e) {
                throw new Error("could not load image \"" + imagePath + "\"", e);
            }
        }
        return images.get(imagePath);
    }
    
    /**
     * Fills a rectangle that has the upper-left corner at (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code> with
     * the current {@linkplain #getColor() color}.
     */
    public void fillRect(double x, double y, double width, double height) {
        withGraphics(g -> g.fill(new Rectangle2D.Double(toNative(x), toNative(y), toNative(width), toNative(height))));
    }
    
    /**
     * Fills an oval with the current {@linkplain #getColor() color}. The oval has a
     * rectangular bounding box with the upper-left corner at (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code>
     */
    public void fillOval(double x, double y, double width, double height) {
        withGraphics(g -> g.fill(new Ellipse2D.Double(toNative(x), toNative(y), toNative(width), toNative(height))));
    }
    
    /**
     * Fills a circle that has the center at (<code>x</code>, <code>y</code>) and
     * the given <code>radius</code> with the current {@linkplain #getColor()
     * color}.
     */
    public void fillCircle(double centerX, double centerY, double radius) {
        fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    private void withGraphics(Consumer<Graphics2D> command) {
        Graphics2D g = canvas.createGraphics();
        g.addRenderingHints(singletonMap(KEY_STROKE_CONTROL, VALUE_STROKE_PURE));
        g.addRenderingHints(singletonMap(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        g.setColor(new java.awt.Color(color.r, color.g, color.b));
        g.setStroke(new BasicStroke((float) (toNative(strokeWidth)),
                roundStroke ? CAP_ROUND : CAP_BUTT,
                roundStroke ? JOIN_ROUND : JOIN_MITER));
        g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN, (float) toNative(fontSize)));
        command.accept(g);
        g.dispose();
    }
    
    /*
     * Input
     */
    
    public List<String> getPressedKeys() {
        return pressedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).key)
                .collect(toList());
    }
    
    public List<String> getTypedKeys() {
        return releasedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).key)
                .collect(toList());
    }
    
    /**
     * Returns whether the key specified by the given <code>keyText</code> is
     * currently pressed. Use {@link #getPressedKeys()} to find out the names
     * for your keys.
     */
    public boolean isKeyPressed(String keyName) {
        return pressedSnapshot.contains(new KeyInput(keyName));
    }

    /**
     * Returns whether the key specified by the given <code>keyText</code> was
     * just typed (released). Use {@link #getPressedKeys()} to find out the names
     * for your keys.
     */
    public boolean wasKeyTyped(String keyName) {
        return releasedSnapshot.contains(new KeyInput(keyName));
    }
    
    /**
     * Returns whether the left mouse button is currently pressed. Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     * 
     * @see #isRightMouseButtonPressed()
     */
    public boolean isLeftMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(true));
    }
    
    /**
     * Returns whether the right mouse button is currently pressed. Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     * 
     * @see #isLeftMouseButtonPressed()
     */
    public boolean isRightMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(false));
    }
    
    /**
     * Returns whether the left mouse button was just clicked (released). Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     * 
     * @see #isRightMouseButtonClicked()
     */
    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }
    
    /**
     * Returns whether the right mouse button was just clicked (released). Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     * 
     * @see #isLeftMouseButtonClicked()
     */
    public boolean wasRightMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(false));
    }
    
    /**
     * Returns the x coordinate of the current mouse position within the
     * window.
     * 
     * @see #getMouseY()
     */
    public double getMouseX() {
        return mouseX;
    }

    /**
     * Returns the y coordinate of the current mouse position within the
     * window.
     * 
     * @see #getMouseX()
     */
    public double getMouseY() {
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
    private double toNative(double pixels) {
        return pixels * pixelScale;
    }
    
    /** Converts the given number of native pixels to "user space" pixels (for high-DPI displays). */
    private double toUser(double pixels) {
        return pixels / pixelScale;
    }
    
    private static class Input {}
    
    private static class KeyInput extends Input {
        String key;
        KeyInput(KeyEvent e) {
           this(code2text.get(e.getKeyCode()));
        }
        KeyInput(String keyText) {
            if(!legalKeyTexts.contains(keyText.toLowerCase()))
                throw new IllegalArgumentException("key \"" + keyText + "\" does not exist");
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

/**
 * A class to represent colors.
 */
final class Color {
    
    public final int r, g, b;
    
    /**
     * Creates a new color. The three parameters represent the red, green, and blue
     * channel and are expected to be in the 0&ndash;255 range. Values outside this
     * range will be clamped.
     */
    public Color(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }
    
    /**
     * Returns an integer representation of this color.
     */
    public int toRgbInt() {
        return r << 16 | g << 8 | b;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + r;
        result = prime * result + g;
        result = prime * result + b;
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Color other = (Color) obj;
        return r == other.r && g == other.g && b == other.b;
    }
}
