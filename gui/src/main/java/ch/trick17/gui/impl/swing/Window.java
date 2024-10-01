package ch.trick17.gui.impl.swing;

import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;
import ch.trick17.gui.impl.GuiBase;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.RenderingHints.*;
import static java.awt.geom.Path2D.WIND_EVEN_ODD;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.swing.SwingUtilities.*;

/**
 * Swing-based implementation of {@link Gui}.
 */
public class Window extends GuiBase {

    private static final Set<String> LEGAL_KEY_NAMES = new HashSet<>();
    private static final Map<Integer, String> CODE_TO_NAME = new HashMap<>();

    static {
        for (var field : KeyEvent.class.getFields()) {
            var fieldName = field.getName();
            if (fieldName.startsWith("VK_")) {
                try {
                    var code = field.getInt(KeyEvent.class);
                    var name = fieldName.substring(3).toLowerCase();
                    LEGAL_KEY_NAMES.add(name);
                    CODE_TO_NAME.put(code, name);
                } catch (Exception ignored) {
                }
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
    private final GraphicsDevice device = getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private boolean fullScreen;

    private List<Consumer<Graphics2D>> drawCommands;
    private List<Consumer<Graphics2D>> drawSnapshot;

    private final Map<String, BufferedImage> images = new HashMap<>();

    public Window(String title, int width, int height) {
        super(title, width, height);

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
                    pressedInputs.add(new MouseInput(isLeftMouseButton(e)));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                var input = new MouseInput(isLeftMouseButton(e));
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
                    pressedInputs.add(toKeyInput(e));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                var input = toKeyInput(e);
                synchronized (inputLock) {
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Window.super.close();
            }
        });
        frame.getContentPane().setBackground(BLACK);
        frame.getContentPane().setLayout(new GridBagLayout());

        var constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        frame.getContentPane().add(panel, constraints);

        drawCommands = new ArrayList<>(List.of(applyCurrentSettings()));
        drawSnapshot = new ArrayList<>();

        var main = Thread.currentThread();
        new Thread(() -> {
            while (true) {
                try {
                    main.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
            invokeLater(frame::dispose);
        }).start();
    }

    private KeyInput toKeyInput(KeyEvent e) {
        var keyName = CODE_TO_NAME.get(e.getKeyCode());
        if (!LEGAL_KEY_NAMES.contains(keyName.toLowerCase())) {
            throw new IllegalArgumentException("key \"" + keyName + "\" does not exist");
        }
        return new KeyInput(keyName, e.getKeyChar());
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
        run(() -> {
            frame.pack();
            frame.setLocationRelativeTo(null); // center
            frame.setVisible(true);
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.setAlwaysOnTop(false);
        });
        super.open();
    }

    @Override
    public void close() {
        super.close();
        run(() -> frame.setVisible(false));
    }

    @Override
    public void setResizable(boolean resizable) {
        run(() -> {
            var layout = (GridBagLayout) frame.getContentPane().getLayout();
            var constraints = layout.getConstraints(panel);
            constraints.fill = resizable
                    ? GridBagConstraints.BOTH
                    : GridBagConstraints.CENTER;
            layout.setConstraints(panel, constraints);
            frame.setResizable(resizable);
        });
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        run(() -> {
            if (fullScreen) {
                frame.dispose();
                frame.setUndecorated(true);
                device.setFullScreenWindow(frame);
            } else {
                device.setFullScreenWindow(null);
                frame.dispose();
                frame.setUndecorated(false);
            }
            frame.setVisible(true);
        });
    }

    @Override
    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    protected void repaint(boolean clear) {
        frame.repaint();
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
    }

    /*
     * Paint settings
     */

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        drawCommands.add(g -> g.setColor(new java.awt.Color(color.r, color.g, color.b, color.alpha)));
    }

    @Override
    public void setStrokeWidth(double strokeWidth) {
        super.setStrokeWidth(strokeWidth);
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke((float) strokeWidth,
                    prev.getEndCap(),
                    prev.getLineJoin()));
        });
    }

    @Override
    public void setRoundStroke(boolean roundStroke) {
        super.setRoundStroke(roundStroke);
        drawCommands.add(g -> {
            var prev = (BasicStroke) g.getStroke();
            g.setStroke(new BasicStroke(prev.getLineWidth(),
                    roundStroke ? CAP_ROUND : CAP_BUTT,
                    roundStroke ? JOIN_ROUND : JOIN_MITER));
        });
    }

    @Override
    public void setFontSize(int fontSize) {
        super.setFontSize(fontSize);
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont((float) fontSize)));
    }

    @Override
    public void setBold(boolean bold) {
        super.setBold(bold);
        drawCommands.add(g -> g.setFont(g.getFont().deriveFont(bold ? BOLD : PLAIN)));
    }

    @Override
    public void setTextAlign(int textAlign) {
        super.setTextAlign(textAlign);
        drawCommands.add(g -> g.addRenderingHints(Map.of(TEXT_ALIGN, TextAlign.fromInt(textAlign))));
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        super.setLineSpacing(lineSpacing);
        drawCommands.add(g -> g.addRenderingHints(Map.of(LINE_SPACING, clampPositive(lineSpacing))));
    }

    @Override
    public void setAlpha(double alpha) {
        super.setAlpha(alpha);
        drawCommands.add(g -> g.setComposite(AlphaComposite.SrcOver.derive((float) max(0, min(1, alpha)))));
    }

    @Override
    public double stringWidth(String string, int fontSize, boolean bold) {
        var font = panel.getFont().deriveFont(bold ? BOLD : PLAIN, fontSize);
        var metrics = panel.getFontMetrics(font);
        return string.lines()
                .mapToInt(metrics::stringWidth)
                .max().orElse(0);
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

    private void ensureLoaded(String imagePath) {
        if (!images.containsKey(imagePath)) {
            try (var res = getClass().getClassLoader().getResourceAsStream(imagePath)) {
                BufferedImage image;
                if (res != null) {
                    image = ImageIO.read(res);
                } else {
                    image = ImageIO.read(new File(imagePath));
                }
                if (image == null) {
                    throw new Error("could not load image \"" + imagePath + "\"");
                }
                images.put(imagePath, image);
            } catch (IOException e) {
                throw new Error("could not load image \"" + imagePath + "\"", e);
            }
        }
    }

    private void run(Runnable run) {
        try {
            invokeAndWait(run);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        } catch (InterruptedException ignored) {
        }
    }
}