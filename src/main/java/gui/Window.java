package gui;

import gui.component.Component;
import gui.component.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static java.awt.BasicStroke.*;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.*;
import static java.lang.Integer.signum;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * <p>
 * A class for creating simple GUIs (graphical user interfaces). Every instance
 * represents a separate Window. The programmer can display content in the
 * window by drawing on a canvas using <code>draw...()</code> and
 * <code>fill...()</code> methods. Note that the content of the canvas is not
 * displayed immediately, but only after a call to {@link #open()} or
 * {@link #refresh(int)}.
 *
 * <p>
 * There are two ways to use this class. The first way is for displaying static
 * content. First, draw the content using the various <code>draw...()</code> or
 * <code>fill...()</code> methods, then, open the window with {@link #open()},
 * and finally, call {@link #waitUntilClosed()}:
 *
 * <pre>
 * Window window = new Window("Pixels", width, height);
 * window.drawString("Hello World!", x, y);
 * window.open();
 * window.waitUntilClosed();</pre>
 *
 * <p>
 * The second way is for displaying dynamic and possibly interactive content.
 * First, open the window and then, draw and call {@link #refresh(int)} in a
 * loop:
 *
 * <pre>
 * Window window = new Window("Pixels", width, height);
 * window.open();
 * while (window.isOpen()) {
 *     window.drawString("Hello World!", x, y);
 *     window.refresh(20);
 * }</pre>
 *
 * <p>
 * All methods of this class use a pixel-based coordinate system with the origin
 * in the upper-left corner of the window. The x-axis extends to the right while
 * the y-axis extends to the bottom of the window.
 */
public class Window {

    private static final Set<String> LEGAL_KEY_TEXTS = new HashSet<>();
    private static final Map<Integer, String> CODE_TO_TEXT = new HashMap<>();

    static {
        for (var field : KeyEvent.class.getFields()) {
            var name = field.getName();
            if (name.startsWith("VK_")) {
                try {
                    var code = field.getInt(KeyEvent.class);
                    var text = name.substring(3).toLowerCase();
                    LEGAL_KEY_TEXTS.add(text);
                    CODE_TO_TEXT.put(code, text);
                } catch (Exception ignored) {}
            }
        }
    }

    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 100;

    private final JFrame frame;
    private final JPanel panel;

    private List<Consumer<Graphics2D>> drawCommands;
    private List<Consumer<Graphics2D>> drawSnapshot;

    private Color color = new Color(0, 0, 0);
    private double strokeWidth = 1;
    private boolean roundStroke = false;
    private int fontSize = 11;
    private boolean bold = false;
    private TextAlign textAlign = TextAlign.LEFT;
    private double alpha = 1;

    private final Map<String, BufferedImage> images = new HashMap<>();

    private final Object inputLock = new Object();
    private final Set<Input> pressedInputs = new HashSet<>();
    private final Set<Input> releasedInputs = new HashSet<>();
    private final Set<Input> pressedSnapshot = new HashSet<>();
    private final Set<Input> releasedSnapshot = new HashSet<>();

    private volatile double mouseX = 0;
    private volatile double mouseY = 0;

    private volatile boolean open = false;
    private volatile double width;
    private volatile double height;

    private long lastRefreshTime = 0;

    private final Set<Hoverable> hovered = newSetFromMap(new IdentityHashMap<>());
    private final List<Component> components = new ArrayList<>();

    /**
     * Create a new window with the specified title, width, and height.
     */
    public Window(String title, int width, int height) {
        this.width = width;
        this.height = height;

        frame = new JFrame();
        frame.setTitle(title);
        frame.setResizable(false);
        frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics graphics) {
                var g = (Graphics2D) graphics;
                g.addRenderingHints(Map.of(
                        KEY_STROKE_CONTROL, VALUE_STROKE_PURE,
                        KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
                // white background
                g.setColor(WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                // execute draw commands
                synchronized (Window.this) {
                    drawSnapshot.forEach(command -> command.accept(g));
                }
            }
        };
        var size = new Dimension(width, height);
        panel.setSize(size);
        panel.setPreferredSize(size);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (inputLock) {
                    pressedInputs.add(new MouseInput(e));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                var input = new MouseInput(e);
                synchronized (inputLock) {
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                var x = e.getX();
                var y = e.getY();
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    mouseX = x;
                    mouseY = y;
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
                synchronized (inputLock) {
                    pressedInputs.add(new KeyInput(e));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                var input = new KeyInput(e);
                synchronized (inputLock) {
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

        drawCommands = new ArrayList<>(List.of(applyCurrentSettings()));
        drawSnapshot = new ArrayList<>();

        var main = Thread.currentThread();
        new Thread(() -> {
            while (true) {
                try {
                    main.join();
                    break;
                } catch (InterruptedException ignored) {}
            }
            invokeLater(frame::dispose);
        }).start();
    }

    private Consumer<Graphics2D> applyCurrentSettings() {
        var currentColor = new java.awt.Color(color.r, color.g, color.b, color.alpha);
        var currentStroke = new BasicStroke((float) strokeWidth, roundStroke ? CAP_ROUND : CAP_BUTT,
                roundStroke ? JOIN_ROUND : JOIN_MITER);
        var currentStyle = bold ? BOLD : PLAIN;
        var currentSize = fontSize;
        var currentTextAlign = textAlign;
        var currentComposite = AlphaComposite.SrcOver.derive((float) alpha);
        return g -> {
            g.setColor(currentColor);
            g.setStroke(currentStroke);
            g.setFont(g.getFont().deriveFont(currentStyle, currentSize));
            // Text alignment is stored as a "rendering hint" inside the
            // Graphics2D object. Somewhat hacky, but consistent with all other
            // settings, which are supported by Graphics2D directly.
            g.addRenderingHints(Map.of(TextAlign.Key.INSTANCE, currentTextAlign));
            g.setComposite(currentComposite);
        };
    }

    /**
     * Opens the window and displays the current content of the canvas.
     */
    public void open() {
        drawSnapshot.addAll(drawCommands);
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
        open = false;
        run(() -> frame.setVisible(false));
    }

    /**
     * Returns <code>true</code> if the window is currently open, <code>false</code>
     * otherwise. Note that the window can be closed either by the programmer (by
     * calling {@link #close()}) or by the user.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * This method waits until the window is closed by the user (or if it was
     * not open in the first place). More precisely, this method returns as soon
     * as {@link #isOpen()} returns <code>true</code>.
     */
    public void waitUntilClosed() {
        while (isOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Repeatedly runs all the {@linkplain #addComponent(Component) registered}
     * components by {@linkplain #refreshAndClear() refreshing and clearing}
     * until the window is closed by the user.
     * 
     * @see #runUntilClosed(int)
     */
    public void runUntilClosed() {
        runUntilClosed(0);
    }

    /**
     * Repeatedly runs all the {@linkplain #addComponent(Component) registered}
     * components by {@linkplain #refreshAndClear(int) refreshing and clearing}
     * until the window is closed by the user. Waits <code>waitTime</code>
     * milliseconds between iterations.
     */
    public void runUntilClosed(int waitTime) {
        while (isOpen()) {
            refreshAndClear(waitTime);
        }
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
     *
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
        runComponents();

        if (clear) {
            synchronized (this) {
                drawSnapshot = drawCommands;
                drawCommands = new ArrayList<>(List.of(applyCurrentSettings()));
            }
        } else {
            synchronized (this) {
                drawSnapshot = new ArrayList<>(drawCommands);
            }
        }

        while (true) {
            var sleepTime = (waitTime - (System.currentTimeMillis() - lastRefreshTime)) / 2;
            try {
                if (sleepTime > 1) {
                    Thread.sleep(sleepTime);
                } else {
                    break;
                }
            } catch (InterruptedException ignored) {}
        }
        lastRefreshTime = System.currentTimeMillis();

        frame.repaint();

        pressedSnapshot.clear();
        releasedSnapshot.clear();
        synchronized (inputLock) {
            pressedSnapshot.addAll(pressedInputs);
            releasedSnapshot.addAll(releasedInputs);
            releasedInputs.clear();
        }
    }

    private void runComponents() {
        var mx = mouseX;
        var my = mouseY;
        for (var comp : components) {
            if (comp instanceof Hoverable) {
                var h = (Hoverable) comp;
                if (h.getInteractiveArea(this).contains(mx, my) && hovered.add(h)) {
                    h.onMouseEnter();
                } else if (!h.getInteractiveArea(this).contains(mx, my) && hovered.remove(h)) {
                    h.onMouseExit();
                }
            }
        }
        var leftClicked = wasLeftMouseButtonClicked();
        var rightClicked = wasRightMouseButtonClicked();
        if (leftClicked || rightClicked) {
            for (var comp : components) {
                if (comp instanceof Clickable) {
                    var c = (Clickable) comp;
                    if (c.getInteractiveArea(this).contains(mx, my)) {
                        if (leftClicked) {
                            c.onLeftClick(mx, my);
                        }
                        if (rightClicked) {
                            c.onRightClick(mx, my);
                        }
                    }
                }
            }
        }
        for (var comp : components) {
            if (comp instanceof Drawable) {
                var d = (Drawable) comp;
                d.draw(this);
            }
        }
    }

    /**
     * If <code>resizable</code> is <code>true</code>, this window can be resized by
     * the user. By default, windows are non-resizable.
     * <p>
     * For resizable windows, use {@link #getWidth()} and {@link #getHeight()} to
     * get the current window size.
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
     * Returns the current window height (excluding any title bar added by the
     * operating system).
     */
    public double getHeight() {
        return height;
    }

    /**
     * Adds <code>component</code> to this window. Whenever one of the
     * {@link #refresh()} methods is called, first the events for
     * {@link Interactive} components (e.g. {@link Hoverable#onMouseEnter()
     * onMouseEnter()}) are fired and then {@link Drawable} components are drawn.
     *
     * @throws IllegalArgumentException if <code>component</code> is
     *                                  <code>null</code> or already added.
     * @see #removeComponent(Component)
     */
    public void addComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        if (components.stream().anyMatch(c -> c == component)) {
            throw new IllegalArgumentException("component already added");
        }
        components.add(component);
    }

    /**
     * Removes <code>component</code> from this window.
     *
     * @throws IllegalArgumentException if <code>component</code> is
     *                                  <code>null</code> not previously added.
     * @see #addComponent(Component)
     */
    public void removeComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        if (!components.remove(component)) {
            throw new IllegalArgumentException("component not present");
        }
    }

    /*
     * Painting
     */

    /**
     * Sets the color for the subsequent drawing operations. The three parameters
     * represent the red, green, and blue channel and are expected to be in the
     * 0&ndash;255 range. Values outside this range will be clamped. The default
     * color is black (0, 0, 0). For colors with transparency, use
     * {@link #setColor(Color)}.
     */
    public void setColor(int red, int green, int blue) {
        setColor(new Color(red, green, blue));
    }

    /**
     * Sets the color for the subsequent drawing operations, using a {@link Color}
     * object. The default color is black (0, 0, 0).
     */
    public void setColor(Color color) {
        this.color = color;
        drawCommands.add(g -> g.setColor(new java.awt.Color(color.r, color.g, color.b, color.alpha)));
    }

    /**
     * Returns the current drawing color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the stroke width for subsequent <code>draw...()</code> operations, in
     * pixels. The default stroke width is 1 pixel.
     */
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke((float) strokeWidth,
                    prev.getEndCap(),
                    prev.getLineJoin()));
        });
    }

    /**
     * Returns the current stroke width (in pixels).
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * If <code>roundStroke</code> is <code>true</code>, subsequent
     * <code>draw...()</code> operations will use round stroke caps and joins
     * instead of flat caps and miter joins.
     */
    public void setRoundStroke(boolean roundStroke) {
        this.roundStroke = roundStroke;
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke(prev.getLineWidth(),
                    roundStroke ? CAP_ROUND : CAP_BUTT,
                    roundStroke ? JOIN_ROUND : JOIN_MITER));
        });
    }

    /**
     * Sets the font size for subsequent {@link #drawString(String, double, double)}
     * operations, in points. The default font size is 11 points.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont((float) fontSize)));
    }

    /**
     * Returns the current font size, in points.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * If <code>bold</code> is <code>true</code>, subsequent
     * {@link #drawString(String, double, double)} operations will use a bold font.
     */
    public void setBold(boolean bold) {
        this.bold = bold;
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN)));
    }

    public boolean isBold() {
        return bold;
    }

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text left aligned.
     */
    public void setTextAlignLeft() {
        setTextAlign(-1);
    }

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text centered (on the x Axis).
     */
    public void setTextAlignCenter() {
        setTextAlign(0);
    }

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text right aligned.
     */
    public void setTextAlignRight() {
        setTextAlign(1);
    }

    /**
     * Sets the alignment for subsequent {@link #drawString(String, double, double)}
     * operations. A negative value means left aligned, zero means centered, and
     * a positive value means right aligned.
     */
    public void setTextAlign(int textAlign) {
        this.textAlign = TextAlign.fromInt(textAlign);
        drawCommands.add(g -> g.addRenderingHints(Map.of(TextAlign.Key.INSTANCE, TextAlign.fromInt(textAlign))));
    }

    /**
     * Returns the current text alignment, as an int. Left aligned is represented
     * as -1, centered as 0, and right aligned as +1.
     */
    public int getTextAlign() {
        return textAlign.toInt();
    }

    public void setAlpha(double alpha) {
        var clamped = max(0, min(1, alpha));
        this.alpha = clamped;
        drawCommands.add(g -> g.setComposite(AlphaComposite.SrcOver.derive((float) clamped)));
    }

    public double getAlpha() {
        return alpha;
    }

    /**
     * Draws the outline of a rectangle with the upper-left corner at
     * (<code>x</code>, <code>y</code>) and the given <code>width</code> and
     * <code>height</code>. The current {@linkplain #getColor() color} and
     * {@linkplain #getStrokeWidth() stroke width} are used.
     */
    public void drawRect(double x, double y, double width, double height) {
        drawCommands.add(g -> g.draw(new Rectangle2D.Double(x, y, width, height)));
    }

    /**
     * Draws the outline of an oval with a rectangular bounding box that has the
     * upper-left corner at (<code>x</code>, <code>y</code>) and the given
     * <code>width</code> and <code>height</code>. The current
     * {@linkplain #getColor() color} and {@linkplain #getStrokeWidth() stroke
     * width} are used.
     */
    public void drawOval(double x, double y, double width, double height) {
        drawCommands.add(g -> g.draw(new Ellipse2D.Double(x, y, width, height)));
    }

    /**
     * Draws the outline of a circle with the center at (<code>x</code>,
     * <code>y</code>) and the given <code>radius</code>. The current
     * {@linkplain #getColor() color} and {@linkplain #getStrokeWidth() stroke
     * width} are used.
     */
    public void drawCircle(double centerX, double centerY, double radius) {
        drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * Draws a line from (<code>x1</code>, <code>y1</code>) to (<code>x2</code>,
     * <code>y2</code>). The current {@linkplain #getColor() color} and
     * {@linkplain #getStrokeWidth() stroke width} are used.
     */
    public void drawLine(double x1, double y1, double x2, double y2) {
        drawCommands.add(g -> g.draw(new Line2D.Double(x1, y1, x2, y2)));
    }

    /**
     * Draws the given string with the current {@linkplain #getColor() color},
     * {@linkplain #getFontSize() font size}, {@linkplain #isBold() boldness},
     * and {@linkplain #getTextAlign() alignment}. The baseline is located at
     * the given <code>y</code> coordinate.
     */
    public void drawString(String string, double x, double y) {
        drawCommands.add(g -> {
            var align = (TextAlign) g.getRenderingHints().get(TextAlign.Key.INSTANCE);
            var metrics = g.getFontMetrics();
            var drawY = y;
            for (var line : (Iterable<String>) string.lines()::iterator) {
                var drawX = x;
                if (align != TextAlign.LEFT) {
                    var width = metrics.stringWidth(line);
                    drawX -= align == TextAlign.CENTER ? width / 2f : width;
                }
                g.drawString(line, (float) drawX, (float) drawY);
                drawY += g.getFont().getSize();
            }
        });
    }

    /**
     * Draws the given string with the current {@linkplain #getColor() color},
     * {@linkplain #getFontSize() font size}, and {@linkplain #isBold() boldness}.
     * The <em>center</em> of the baseline is at position (<code>x</code>,
     * <code>y</code>).
     *
     * @deprecated Provided for backwards compatibility. The methods
     * {@link #setTextAlignCenter()} and {@link #setTextAlignRight()}, etc. provide
     * more flexibility and consistency. This method ignores the text alignment
     * setting defined using those methods and does not support multi-line text.
     */
    @Deprecated
    public void drawStringCentered(String string, double x, double y) {
        drawCommands.add(g -> {
            var metrics = g.getFontMetrics();
            var width = metrics.stringWidth(string);
            g.drawString(string, (float) x - width / 2f, (float) y);
        });
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
        drawImage(path, x, y, 1);
    }

    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y) {
        drawImageCentered(path, x, y, 1);
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
     * (<code>x</code>, <code>y</code>) and scales it by the given
     * <code>scale</code>. For example, a scale of 2.0 doubles the size of the
     * image.
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y, double scale) {
        drawImageCentered(path, x, y, scale, 0);
    }

    public void drawImage(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        var image = images.get(path);
        var transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(scale, scale);
        transform.rotate(angle, image.getWidth() / 2.0, image.getHeight() / 2.0);
        drawCommands.add(g -> g.drawImage(image, transform, null));
    }

    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>), scales it by the given <code>scale</code>
     * and rotates it by the given <code>angle</code>, in radians
     * (0&ndash;2&times;{@linkplain Math#PI &pi;}).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    public void drawImageCentered(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        var image = images.get(path);
        var transform = new AffineTransform();
        transform.translate(x - image.getWidth() / 2.0 * scale,
                            y - image.getHeight() / 2.0 * scale);
        transform.scale(scale, scale);
        transform.rotate(angle, image.getWidth() / 2.0, image.getHeight() / 2.0);
        drawCommands.add(g -> g.drawImage(image, transform, null));
    }

    private void ensureLoaded(String imagePath) throws Error {
        if (!images.containsKey(imagePath)) {
            try {
                var image = ImageIO.read(new File(imagePath));
                if (image == null) {
                    throw new Error("could not load image \"" + imagePath + "\"");
                }
                images.put(imagePath, image);
            } catch (IOException e) {
                throw new Error("could not load image \"" + imagePath + "\"", e);
            }
        }
    }

    /**
     * Fills a rectangle that has the upper-left corner at (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code> with
     * the current {@linkplain #getColor() color}.
     */
    public void fillRect(double x, double y, double width, double height) {
        drawCommands.add(g -> g.fill(new Rectangle2D.Double(x, y, width, height)));
    }

    /**
     * Fills an oval with the current {@linkplain #getColor() color}. The oval has a
     * rectangular bounding box with the upper-left corner at (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code>
     */
    public void fillOval(double x, double y, double width, double height) {
        drawCommands.add(g -> g.fill(new Ellipse2D.Double(x, y, width, height)));
    }

    /**
     * Fills a circle that has the center at (<code>x</code>, <code>y</code>) and
     * the given <code>radius</code> with the current {@linkplain #getColor()
     * color}.
     */
    public void fillCircle(double centerX, double centerY, double radius) {
        fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
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
     * currently pressed. Use {@link #getPressedKeys()} to find out the names for
     * your keys.
     */
    public boolean isKeyPressed(String keyName) {
        return pressedSnapshot.contains(new KeyInput(keyName));
    }

    /**
     * Returns whether the key specified by the given <code>keyText</code> was just
     * typed (released). Use {@link #getPressedKeys()} to find out the names for
     * your keys.
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
     * @see #wasRightMouseButtonClicked()
     */
    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }

    /**
     * Returns whether the right mouse button was just clicked (released). Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     *
     * @see #wasLeftMouseButtonClicked()
     */
    public boolean wasRightMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(false));
    }

    /**
     * Returns the x coordinate of the current mouse position within the window.
     *
     * @see #getMouseY()
     */
    public double getMouseX() {
        return mouseX;
    }

    /**
     * Returns the y coordinate of the current mouse position within the window.
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
        } catch (InterruptedException ignored) {}
    }

    private static class Input {}

    private static class KeyInput extends Input {
        String key;

        KeyInput(KeyEvent e) {
            this(CODE_TO_TEXT.get(e.getKeyCode()));
        }

        KeyInput(String keyText) {
            if (!LEGAL_KEY_TEXTS.contains(keyText.toLowerCase())) {
                throw new IllegalArgumentException("key \"" + keyText + "\" does not exist");
            }
            this.key = keyText.toLowerCase();
        }

        @Override
        public int hashCode() {
            return 31 + key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof KeyInput && key.equals(((KeyInput) obj).key);
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
            return this == obj || obj instanceof MouseInput && left == ((MouseInput) obj).left;
        }
    }

    private enum TextAlign {
        LEFT, CENTER, RIGHT;

        static TextAlign fromInt(int textAlign) {
            return values()[signum(textAlign) + 1];
        }

        int toInt() {
            return ordinal() - 1;
        }

        static class Key extends RenderingHints.Key {
            static final Key INSTANCE = new Key();

            private Key() {
                super(Integer.MAX_VALUE / 13);
            }

            public boolean isCompatibleValue(Object val) {
                return val instanceof TextAlign;
            }
        }
    }
}