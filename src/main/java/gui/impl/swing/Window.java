package gui.impl.swing;

import gui.Color;
import gui.Gui;
import gui.component.Clickable;
import gui.component.Component;
import gui.component.Drawable;
import gui.component.Hoverable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
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
import static java.awt.geom.Path2D.WIND_EVEN_ODD;
import static java.lang.Double.isFinite;
import static java.lang.Integer.signum;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Swing-based implementation of {@link Gui}.
 */
public class Window implements Gui {

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

    private static final Key TEXT_ALIGN = new Key(165191049) {
        public boolean isCompatibleValue(Object val) {
            return val instanceof TextAlign;
        }
    };
    private static final Key LINE_SPACING = new Key(165191050) {
        public boolean isCompatibleValue(Object val) {
            return val instanceof Double;
        }
    };

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
    private double lineSpacing = 1.0;
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
        var currentLineHeight = lineSpacing;
        var currentComposite = AlphaComposite.SrcOver.derive((float) alpha);
        return g -> {
            g.setColor(currentColor);
            g.setStroke(currentStroke);
            g.setFont(g.getFont().deriveFont(currentStyle, currentSize));
            // Text alignment and line height are stored as a "rendering hints"
            // inside the Graphics2D object. Somewhat hacky, but consistent with
            // all other settings, which are supported by Graphics2D directly.
            g.addRenderingHints(Map.of(TEXT_ALIGN, currentTextAlign));
            g.addRenderingHints(Map.of(LINE_SPACING, currentLineHeight));
            g.setComposite(currentComposite);
        };
    }

    @Override
    public void open() {
        drawSnapshot.addAll(drawCommands);
        lastRefreshTime = System.currentTimeMillis();
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

    @Override
    public void close() {
        open = false;
        run(() -> frame.setVisible(false));
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void waitUntilClosed() {
        while (isOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void runUntilClosed() {
        runUntilClosed(0);
    }

    @Override
    public void runUntilClosed(int waitTime) {
        while (isOpen()) {
            refreshAndClear(waitTime);
        }
    }

    @Override
    public void refresh() {
        refresh(0);
    }

    @Override
    public void refresh(int waitTime) {
        refresh(waitTime, false);
    }

    @Override
    public void refreshAndClear() {
        refreshAndClear(0);
    }

    @Override
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

    @Override
    public void setResizable(boolean resizable) {
        run(() -> frame.setResizable(resizable));
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void addComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        if (components.stream().anyMatch(c -> c == component)) {
            throw new IllegalArgumentException("component already added");
        }
        components.add(component);
    }

    @Override
    public void removeComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        if (!components.remove(component)) {
            throw new IllegalArgumentException("component not present");
        }
    }

    /*
     * Paint settings
     */

    @Override
    public void setColor(int red, int green, int blue) {
        setColor(new Color(red, green, blue));
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
        drawCommands.add(g -> g.setColor(new java.awt.Color(color.r, color.g, color.b, color.alpha)));
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke((float) strokeWidth,
                    prev.getEndCap(),
                    prev.getLineJoin()));
        });
    }

    @Override
    public double getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public void setRoundStroke(boolean roundStroke) {
        this.roundStroke = roundStroke;
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke(prev.getLineWidth(),
                    roundStroke ? CAP_ROUND : CAP_BUTT,
                    roundStroke ? JOIN_ROUND : JOIN_MITER));
        });
    }

    @Override
    public boolean isRoundStroke() {
        return roundStroke;
    }

    @Override
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont((float) fontSize)));
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setBold(boolean bold) {
        this.bold = bold;
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN)));
    }

    @Override
    public boolean isBold() {
        return bold;
    }

    @Override
    public double stringWidth(String string) {
        var font = panel.getFont().deriveFont(bold ? BOLD : PLAIN, fontSize);
        var metrics = panel.getFontMetrics(font);
        return string.lines()
                .mapToInt(metrics::stringWidth)
                .max().orElse(0);
    }

    @Override
    public void setTextAlignLeft() {
        setTextAlign(-1);
    }

    @Override
    public void setTextAlignCenter() {
        setTextAlign(0);
    }

    @Override
    public void setTextAlignRight() {
        setTextAlign(1);
    }

    @Override
    public void setTextAlign(int textAlign) {
        this.textAlign = TextAlign.fromInt(textAlign);
        drawCommands.add(g -> g.addRenderingHints(Map.of(TEXT_ALIGN, TextAlign.fromInt(textAlign))));
    }

    @Override
    public int getTextAlign() {
        return textAlign.toInt();
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        var clamped = clampPositive(lineSpacing);
        this.lineSpacing = clamped;
        drawCommands.add(g -> g.addRenderingHints(Map.of(LINE_SPACING, clamped)));
    }

    private double clampPositive(double d) {
        if (isFinite(d)) {
            return max(0, d);
        } else {
            return Double.MAX_VALUE;
        }
    }

    @Override
    public double getLineSpacing() {
        return lineSpacing;
    }

    @Override
    public void setAlpha(double alpha) {
        var clamped = max(0, min(1, alpha));
        this.alpha = clamped;
        drawCommands.add(g -> g.setComposite(AlphaComposite.SrcOver.derive((float) clamped)));
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    /*
     * Painting
     */

    @Override
    public void drawRect(double x, double y, double width, double height) {
        drawCommands.add(g -> g.draw(new Rectangle2D.Double(x, y, width, height)));
    }

    @Override
    public void fillRect(double x, double y, double width, double height) {
        drawCommands.add(g -> g.fill(new Rectangle2D.Double(x, y, width, height)));
    }

    @Override
    public void drawOval(double x, double y, double width, double height) {
        drawCommands.add(g -> g.draw(new Ellipse2D.Double(x, y, width, height)));
    }

    @Override
    public void fillOval(double x, double y, double width, double height) {
        drawCommands.add(g -> g.fill(new Ellipse2D.Double(x, y, width, height)));
    }

    @Override
    public void drawCircle(double centerX, double centerY, double radius) {
        drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    @Override
    public void fillCircle(double centerX, double centerY, double radius) {
        fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        drawCommands.add(g -> g.draw(new Line2D.Double(x1, y1, x2, y2)));
    }

    @Override
    public void drawPath(double[] coordinates) {
        if (coordinates.length >= 2) {
            var path = new Path2D.Double();
            append(path, coordinates);
            drawCommands.add(g -> g.draw(path));
        }
    }

    @Override
    public void drawPolygon(double[] coordinates) {
        if (coordinates.length >= 2) {
            var path = new Path2D.Double();
            append(path, coordinates);
            path.closePath();
            drawCommands.add(g -> g.draw(path));
        }
    }

    @Override
    public void fillPolygon(double[] coordinates) {
        if (coordinates.length >= 2) {
            var path = new Path2D.Double(WIND_EVEN_ODD);
            append(path, coordinates);
            path.closePath();
            drawCommands.add(g -> g.fill(path));
        }
    }

    @Override
    public void drawMultiPolygon(double[][] rings) {
        var path = new Path2D.Double(WIND_EVEN_ODD);
        for (var ring : rings) {
            if (ring.length >= 2) {
                append(path, ring);
                path.closePath();
            }
        }
        drawCommands.add(g -> g.draw(path));
    }

    @Override
    public void fillMultiPolygon(double[][] rings) {
        var path = new Path2D.Double(WIND_EVEN_ODD);
        for (var ring : rings) {
            if (ring.length >= 2) {
                append(path, ring);
                path.closePath();
            }
        }
        drawCommands.add(g -> g.fill(path));
    }

    private static void append(Path2D.Double path, double[] coordinates) {
        path.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 2) {
            path.lineTo(coordinates[i], coordinates[i + 1]);
        }
    }

    @Override
    public void drawString(String string, double x, double y) {
        drawCommands.add(g -> {
            var align = (TextAlign) g.getRenderingHints().get(TEXT_ALIGN);
            var lineHeight = (double) g.getRenderingHints().get(LINE_SPACING);
            var metrics = g.getFontMetrics();
            var drawY = y;
            for (var line : (Iterable<String>) string.lines()::iterator) {
                var drawX = x;
                if (align != TextAlign.LEFT) {
                    var width = metrics.stringWidth(line);
                    drawX -= align == TextAlign.CENTER ? width / 2f : width;
                }
                g.drawString(line, (float) drawX, (float) drawY);
                drawY += g.getFont().getSize() * lineHeight;
            }
        });
    }

    @Override
    @Deprecated
    public void drawStringCentered(String string, double x, double y) {
        drawCommands.add(g -> {
            var metrics = g.getFontMetrics();
            var width = metrics.stringWidth(string);
            g.drawString(string, (float) x - width / 2f, (float) y);
        });
    }

    @Override
    public void drawImage(String path, double x, double y) {
        drawImage(path, x, y, 1);
    }

    @Override
    public void drawImageCentered(String path, double x, double y) {
        drawImageCentered(path, x, y, 1);
    }

    @Override
    public void drawImage(String path, double x, double y, double scale) {
        drawImage(path, x, y, scale, 0);
    }

    @Override
    public void drawImageCentered(String path, double x, double y, double scale) {
        drawImageCentered(path, x, y, scale, 0);
    }

    @Override
    public void drawImage(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        var image = images.get(path);
        var transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(scale, scale);
        transform.rotate(angle, image.getWidth() / 2.0, image.getHeight() / 2.0);
        drawCommands.add(g -> g.drawImage(image, transform, null));
    }

    @Override
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

    /*
     * Input
     */

    @Override
    public List<String> getPressedKeys() {
        return pressedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).key)
                .collect(toList());
    }

    @Override
    public List<String> getTypedKeys() {
        return releasedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).key)
                .collect(toList());
    }

    @Override
    public boolean isKeyPressed(String keyName) {
        return pressedSnapshot.contains(new KeyInput(keyName));
    }

    @Override
    public boolean wasKeyTyped(String keyName) {
        return releasedSnapshot.contains(new KeyInput(keyName));
    }

    @Override
    public boolean isLeftMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(true));
    }

    @Override
    public boolean isRightMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(false));
    }

    @Override
    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }

    @Override
    public boolean wasRightMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(false));
    }

    @Override
    public double getMouseX() {
        return mouseX;
    }

    @Override
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
    }
}