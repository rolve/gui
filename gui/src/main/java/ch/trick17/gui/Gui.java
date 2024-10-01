package ch.trick17.gui;

import ch.trick17.gui.component.Component;
import ch.trick17.gui.component.Drawable;
import ch.trick17.gui.component.Hoverable;
import ch.trick17.gui.component.Interactive;
import ch.trick17.gui.impl.swing.Window;
import ch.trick17.gui.impl.web.WebGui;
import ch.trick17.gui.impl.web.WebGuiServer;

import java.util.List;

/**
 * <p>
 * An interface for creating simple GUIs (graphical user interfaces). By
 * default, each GUI is displayed as a separate window. The programmer can
 * display content in the GUI by drawing on a canvas using
 * <code>draw...()</code> and
 * <code>fill...()</code> methods. Note that the content of the canvas is not
 * displayed immediately, but only after a call to {@link #open()} or
 * {@link #refresh(int)}.
 *
 * <p>
 * There are two ways to use this interface. The first way is for displaying
 * static content. First, draw the content using the various
 * <code>draw...()</code> or
 * <code>fill...()</code> methods, then, open the GUI with {@link #open()},
 * and finally, call {@link #waitUntilClosed()}:
 *
 * <pre>
 * Gui gui = Gui.create("Hello", width, height);
 * gui.drawString("Hello, World!", x, y);
 * gui.open();
 * gui.waitUntilClosed();
 * </pre>
 *
 * <p>
 * The second way is for displaying dynamic and possibly interactive content.
 * First, open the GUI and then, draw and call {@link #refresh(int)} in a loop:
 *
 * <pre>
 * Gui gui = Gui.create("Hello", width, height);
 * gui.open();
 * while (gui.isOpen()) {
 *     gui.drawString("Hello, World!", x, y);
 *     gui.refresh(20);
 * }
 * </pre>
 *
 * <p>
 * All methods of this interface use a pixel-based coordinate system with the
 * origin in the upper-left corner of the GUI. The x-axis extends to the right
 * while the y-axis extends to the bottom of the GUI.
 */
public interface Gui {

    /**
     * Create a new GUI with the specified title, width, and height. By default,
     * the GUI is displayed in a window. If the system property
     * "<code>gui.port</code>" is set, the GUI can be accessed using a web
     * browser instead. For example, when running the program with
     * <code>-Dgui.port=8080</code>, the GUI can be accessed at
     * <a href="http://localhost:8080">http://localhost:8080</a>.
     */
    static Gui create(String title, int width, int height) {
        if (WebGuiServer.port() != null) {
            return new WebGui(title, width, height);
        } else {
            return new Window(title, width, height);
        }
    }

    /**
     * Opens the GUI and displays the current content of the canvas.
     */
    void open();

    /**
     * Closes the GUI.
     */
    void close();

    /**
     * Returns <code>true</code> if the GUI is currently open,
     * <code>false</code> otherwise. Note that the GUI can be closed either by
     * the programmer (by calling {@link #close()}) or by the user.
     */
    boolean isOpen();

    /**
     * This method waits until the GUI is closed by the user (or if it was not
     * open in the first place). More precisely, this method returns as soon as
     * {@link #isOpen()} returns <code>true</code>.
     */
    default void waitUntilClosed() {
        while (isOpen()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Repeatedly runs all the {@linkplain #addComponent(Component) registered}
     * components by {@linkplain #refreshAndClear() refreshing and clearing}
     * until the GUI is closed by the user.
     *
     * @see #runUntilClosed(int)
     */
    default void runUntilClosed() {
        runUntilClosed(0);
    }

    /**
     * Repeatedly runs all the {@linkplain #addComponent(Component) registered}
     * components by {@linkplain #refreshAndClear(int) refreshing and clearing}
     * until the GUI is closed by the user. Waits <code>waitTime</code>
     * milliseconds between iterations.
     */
    default void runUntilClosed(int waitTime) {
        while (isOpen()) {
            refreshAndClear(waitTime);
        }
    }

    /**
     * Displays the current content of the canvas. Use this method in a loop,
     * together with {@link #isOpen()}:
     *
     * <pre>
     * while(gui.isOpen()) {
     *     ...
     *     gui.refresh();
     * }
     * </pre>
     * <p>
     * In addition, this method also clears the <code>was...Pressed()</code>
     * and
     * <code>was...Clicked()</code> input events.
     * <p>
     * Note that this method is equivalent to {@link #refresh(int) refresh(0)}.
     *
     * @see #refreshAndClear()
     */
    default void refresh() {
        refresh(0);
    }

    /**
     * Displays the current content of the canvas. To achieve a constant time
     * interval between iterations, this method does not return until the given
     * <code>waitTime</code> (in milliseconds) has elapsed since the last
     * refresh. For example, to get a frame rate of 50 frames per second, use a
     * <code>waitTime</code> of <code>1000 / 50 = 20</code> milliseconds:
     *
     * <pre>
     * while(gui.isOpen()) {
     *     ...
     *     gui.refresh(20);
     * }
     * </pre>
     * <p>
     * In addition, this method also clears the <code>was...Pressed()</code>
     * and
     * <code>was...Clicked()</code> input events.
     *
     * @see #refreshAndClear(int)
     */
    void refresh(int waitTime);

    /**
     * Displays the current content of the canvas, clears the
     * <code>was...Pressed()</code> and <code>was...Clicked()</code> input
     * events, and then clears the canvas for the next iteration. Call this
     * method instead of {@link #refresh()} if every frame is drawn from
     * scratch.
     * <p>
     * Note that this method is equivalent to
     * {@link #refreshAndClear(int) refreshAndClear(0)}.
     */
    default void refreshAndClear() {
        refreshAndClear(0);
    }

    /**
     * Displays the current content of the canvas, clears the
     * <code>was...Pressed()</code> and <code>was...Clicked()</code> input
     * events, and then clears the canvas for the next iteration. Call this
     * method instead of {@link #refresh(int)} if every frame is drawn from
     * scratch. To achieve a constant time interval between iterations, this
     * method does not return until the given <code>waitTime</code> (in
     * milliseconds) has elapsed since the last refresh.
     */
    void refreshAndClear(int waitTime);

    /**
     * If <code>resizable</code> is <code>true</code>, this GUI can be resized
     * by the user. By default, GUIs are non-resizable.
     * <p>
     * For resizable GUIs, use {@link #getWidth()} and {@link #getHeight()} to
     * get the current GUI size.
     *
     * @throws UnsupportedOperationException if the GUI implementation does not
     *                                       support resizability. This is
     *                                       currently the case for Web GUIs.
     */
    void setResizable(boolean resizable) throws UnsupportedOperationException;

    /**
     * Returns the current canvas width.
     */
    double getWidth();

    /**
     * Returns the current canvas height.
     */
    double getHeight();

    /**
     * Adds <code>component</code> to this GUI. Whenever one of the
     * {@link #refresh()} methods is called, first the events for
     * {@link Interactive} components (e.g.
     * {@link Hoverable#onMouseEnter() onMouseEnter()}) are fired and then
     * {@link Drawable} components are drawn.
     *
     * @throws IllegalArgumentException if <code>component</code> is
     *                                  <code>null</code> or already added.
     * @see #removeComponent(Component)
     */
    void addComponent(Component component);

    default void addComponents(Component... components) {
        for (var c : components) {
            addComponent(c);
        }
    }

    /**
     * Removes <code>component</code> from this GUI.
     *
     * @throws IllegalArgumentException if <code>component</code> is
     *                                  <code>null</code> not previously added.
     * @see #addComponent(Component)
     */
    void removeComponent(Component component);

    default void removeComponents(Component... components) {
        for (var c : components) {
            removeComponent(c);
        }
    }

    /*
     * Paint settings
     */

    /**
     * Sets the color for the subsequent drawing operations. The three
     * parameters represent the red, green, and blue channel and are expected to
     * be in the 0&ndash;255 range. Values outside this range will be clamped.
     * The default color is black (0, 0, 0). For colors with transparency, use
     * {@link #setColor(Color)}.
     */
    default void setColor(int red, int green, int blue) {
        setColor(new Color(red, green, blue));
    }

    /**
     * Sets the color for the subsequent drawing operations, using a
     * {@link Color} object. The default color is black (0, 0, 0).
     */
    void setColor(Color color);

    /**
     * Returns the current drawing color.
     */
    Color getColor();

    /**
     * Sets the stroke width for subsequent <code>draw...()</code> operations,
     * in pixels. The default stroke width is 1 pixel.
     */
    void setStrokeWidth(double strokeWidth);

    /**
     * Returns the current stroke width (in pixels).
     */
    double getStrokeWidth();

    /**
     * If <code>roundStroke</code> is <code>true</code>, subsequent
     * <code>draw...()</code> operations will use round stroke caps and joins
     * (instead of flat caps and miter joins).
     */
    void setRoundStroke(boolean roundStroke);

    /**
     * Returns a boolean value indicating whether round stroke caps and joins
     * are used to draw shapes (instead of flat caps and miter joins). The
     * default is <code>false</code>.
     */
    boolean isRoundStroke();

    /**
     * Sets the font size for subsequent
     * {@link #drawString(String, double, double)} operations, in points. The
     * default font size is 11 points.
     */
    void setFontSize(int fontSize);

    /**
     * Returns the current font size, in points.
     */
    int getFontSize();

    /**
     * If <code>bold</code> is <code>true</code>, subsequent
     * {@link #drawString(String, double, double)} operations will use a bold
     * font.
     */
    void setBold(boolean bold);

    /**
     * Returns a boolean value indicating whether a bold font is used to
     * {@linkplain #drawString(String, double, double) draw strings}. The
     * default is <code>false</code>.
     */
    boolean isBold();

    /**
     * Measures the width that the given text would have if it was
     * {@linkplain #drawString(String, double, double) drawn} with the current
     * {@linkplain #getFontSize() font size} and
     * {@linkplain #isBold() boldness}. If the text contains multiple lines, the
     * width of the widest line is returned.
     *
     * @throws UnsupportedOperationException if the GUI implementation does not
     *                                       support measuring string widths.
     *                                       This is currently the case for Web
     *                                       GUIs.
     */
    default double stringWidth(String string) throws UnsupportedOperationException {
        return stringWidth(string, getFontSize(), isBold());
    }

    /**
     * Measures the width that the given text would have if it was
     * {@linkplain #drawString(String, double, double) drawn} with the given
     * font size and boldness. If the text contains multiple lines, the width of
     * the widest line is returned.
     *
     * @throws UnsupportedOperationException if the GUI implementation does not
     *                                       support measuring string widths.
     *                                       This is currently the case for Web
     *                                       GUIs.
     */
    double stringWidth(String string, int fontSize, boolean bold) throws UnsupportedOperationException;

    /**
     * Sets the alignment for subsequent
     * {@link #drawString(String, double, double)} operations. A negative value
     * means left aligned, zero means centered, and a positive value means right
     * aligned. The default alignment is left.
     */
    void setTextAlign(int textAlign);

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text left aligned.
     */
    default void setTextAlignLeft() {
        setTextAlign(-1);
    }

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text centered (on the x Axis).
     */
    default void setTextAlignCenter() {
        setTextAlign(0);
    }

    /**
     * Subsequent {@link #drawString(String, double, double)} operations will
     * draw the text right aligned.
     */
    default void setTextAlignRight() {
        setTextAlign(1);
    }

    /**
     * Returns the current text alignment, as an int. Left aligned is
     * represented as -1, centered as 0, and right aligned as +1.
     */
    int getTextAlign();

    /**
     * Sets the line spacing for subsequent
     * {@link #drawString(String, double, double)} operations with multiple
     * lines of text. The line spacing is specified as a multiplier of the font
     * size; for example, 1.0 (the default value) means single spacing, 2.0
     * means double spacing, etc.
     */
    void setLineSpacing(double lineSpacing);

    double getLineSpacing();

    /**
     * Sets the alpha value for subsequent drawing operations. The alpha value
     * is a double in the 0&ndash;1 range, where 0 means fully transparent and 1
     * means fully opaque. The default alpha value is 1.
     */
    void setAlpha(double alpha);

    double getAlpha();

    /**
     * Resets all settings (color, font size, etc.) to their default values.
     */
    default void resetSettings() {
        setColor(0, 0, 0);
        setStrokeWidth(1);
        setRoundStroke(false);
        setFontSize(11);
        setBold(false);
        setTextAlignLeft();
        setLineSpacing(1);
        setAlpha(1);
    }

    /**
     * Draws the outline of a rectangle with the upper-left corner at
     * (<code>x</code>, <code>y</code>) and the given <code>width</code> and
     * <code>height</code>. The current {@linkplain #getColor() color},
     * {@linkplain #getStrokeWidth() stroke width}, and
     * {@linkplain #isRoundStroke()  stroke roundness} are used.
     */
    void drawRect(double x, double y, double width, double height);

    /**
     * Fills a rectangle that has the upper-left corner at (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code>
     * with the current {@linkplain #getColor() color}.
     */
    void fillRect(double x, double y, double width, double height);

    /**
     * Draws the outline of an oval with a rectangular bounding box that has the
     * upper-left corner at (<code>x</code>, <code>y</code>) and the given
     * <code>width</code> and <code>height</code>. The current
     * {@linkplain #getColor() color} and
     * {@linkplain #getStrokeWidth() stroke width} are used.
     */
    void drawOval(double x, double y, double width, double height);

    /**
     * Fills an oval with the current {@linkplain #getColor() color}. The oval
     * has a rectangular bounding box with the upper-left corner at
     * (<code>x</code>,
     * <code>y</code>) and the given <code>width</code> and <code>height</code>
     */
    void fillOval(double x, double y, double width, double height);

    /**
     * Draws the outline of a circle with the center at (<code>x</code>,
     * <code>y</code>) and the given <code>radius</code>. The current
     * {@linkplain #getColor() color} and
     * {@linkplain #getStrokeWidth() stroke width} are used.
     */
    default void drawCircle(double centerX, double centerY, double radius) {
        drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * Fills a circle that has the center at (<code>x</code>, <code>y</code>)
     * and the given <code>radius</code> with the current
     * {@linkplain #getColor() color}.
     */
    default void fillCircle(double centerX, double centerY, double radius) {
        fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * Draws a line from (<code>x1</code>, <code>y1</code>) to
     * (<code>x2</code>,
     * <code>y2</code>). The current {@linkplain #getColor() color},
     * {@linkplain #getStrokeWidth() stroke width}, and
     * {@linkplain #isRoundStroke()  stroke roundness} are used.
     */
    void drawLine(double x1, double y1, double x2, double y2);

    /**
     * Draws a path defined by the coordinates in the given array. The odd
     * indices correspond to the x coordinates, the even indices to the y
     * coordinates of the points that constitute the path. For example, if the
     * array <code>{1.0, 2.0, 3.0, 4.0, 5.0, 6.0}</code> is given, the path goes
     * from (1.0, 2.0) to (3.0, 4.0) to (5.0, 6.0).
     * <p>
     * The current {@linkplain #getColor() color},
     * {@linkplain #getStrokeWidth() stroke width}, and
     * {@linkplain #isRoundStroke()  stroke roundness} are used.
     */
    void drawPath(double[] coordinates);

    /**
     * Draws a polygon with a single "ring" defined by the coordinates in the
     * given array. The odd indices correspond to the x coordinates, the even
     * indices to the y coordinates of the corners of the polygon. For example,
     * if the array <code>{1.0, 2.0, 3.0, 4.0, 5.0, 6.0}</code> is given, the
     * polygon is a triangle with the corners at the points (1.0, 2.0), (3.0,
     * 4.0), and (5.0, 6.0).
     * <p>
     * This method is similar to {@link #drawPath(double[]) drawPath}, but
     * always draws a closed path.
     * <p>
     * The current {@linkplain #getColor() color},
     * {@linkplain #getStrokeWidth() stroke width}, and
     * {@linkplain #isRoundStroke()  stroke roundness} are used.
     */
    void drawPolygon(double[] coordinates);

    /**
     * Fills a polygon with a single "ring" defined by the coordinates in the
     * given array, with the current {@linkplain #getColor() color}. The odd
     * indices correspond to the x coordinates, the even indices to the y
     * coordinates of the corners of the polygon. For example, if the array
     * <code>{1.0, 2.0, 3.0, 4.0, 5.0, 6.0}</code> is given, the polygon is a
     * triangle with the corners at the points (1.0, 2.0), (3.0, 4.0), and (5.0,
     * 6.0).
     */
    void fillPolygon(double[] coordinates);

    /**
     * Draws a polygon with multiple "rings" defined by the coordinates in the
     * given 2D array. Each row in the array corresponds to a ring; the odd
     * indices in a row correspond to the x coordinates, the even indices to the
     * y coordinates of the corners of the rings. For example, if the array
     * <code>{{0.0, 0.0, 5.0, 0.0, 2.5, 5.0}, {1.0, 1.0, 4.0, 1.0, 2.5,
     * 4.0}}</code> is given, the polygon has a triangular exterior ring and a
     * smaller triangular hole.
     * <p>
     * The current {@linkplain #getColor() color},
     * {@linkplain #getStrokeWidth() stroke width}, and
     * {@linkplain #isRoundStroke()  stroke roundness} are used.
     */
    void drawMultiPolygon(double[][] rings);

    /**
     * Fills a polygon with multiple "rings" defined by the coordinates in the
     * given 2D array, with the current {@linkplain #getColor() color}. Each row
     * in the array corresponds to a ring; the odd indices in a row correspond
     * to the x coordinates, the even indices to the y coordinates of the
     * corners of the rings. For example, if the array
     * <code>{{0.0, 0.0, 5.0, 0.0, 2.5, 5.0}, {1.0, 1.0, 4.0, 1.0, 2.5,
     * 4.0}}</code> is given, the polygon has a triangular exterior ring and a
     * smaller triangular hole.
     * <p>
     * This method also allows to fill polygons consisting of multiple
     * non-overlapping parts, but as long as these contain no holes, multiple
     * {@link #fillPolygon(double[])} calls could just as well be used.
     */
    void fillMultiPolygon(double[][] rings);

    /**
     * Draws the given string with the current {@linkplain #getColor() color},
     * {@linkplain #getFontSize() font size}, {@linkplain #isBold() boldness},
     * and {@linkplain #getTextAlign() alignment}. The baseline is located at
     * the given <code>y</code> coordinate.
     */
    void drawString(String string, double x, double y);

    /**
     * Draws the image found at the given <code>path</code> with the upper-left
     * corner at position (<code>x</code>, <code>y</code>).
     */
    default void drawImage(String path, double x, double y) {
        drawImage(path, x, y, 1);
    }

    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    default void drawImageCentered(String path, double x, double y) {
        drawImageCentered(path, x, y, 1);
    }

    /**
     * Draws the image found at the given <code>path</code> with the upper-left
     * corner at position (<code>x</code>, <code>y</code>) and scales it by the
     * given <code>scale</code>. For example, a scale of 2.0 doubles the size of
     * the image.
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    default void drawImage(String path, double x, double y, double scale) {
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
    default void drawImageCentered(String path, double x, double y, double scale) {
        drawImageCentered(path, x, y, scale, 0);
    }

    void drawImage(String path, double x, double y, double scale, double angle);

    /**
     * Draws the image found at the given path with the center at position
     * (<code>x</code>, <code>y</code>), scales it by the given
     * <code>scale</code> and rotates it by the given <code>angle</code>, in
     * radians (0&ndash;2&times;{@linkplain Math#PI &pi;}).
     * <p>
     * Also, see {@link #drawImage(String, double, double)}.
     */
    void drawImageCentered(String path, double x, double y, double scale, double angle);

    List<String> getPressedKeys();

    List<String> getTypedKeys();

    /**
     * Returns whether the key specified by the given <code>keyText</code> is
     * currently pressed. Use {@link #getPressedKeys()} to find out the names
     * for your keys.
     */
    boolean isKeyPressed(String keyName);

    /**
     * Returns whether the key specified by the given <code>keyText</code> was
     * just typed (released). Use {@link #getPressedKeys()} to find out the
     * names for your keys.
     */
    boolean wasKeyTyped(String keyName);

    /**
     * Returns whether the left mouse button is currently pressed. Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     *
     * @see #isRightMouseButtonPressed()
     */
    boolean isLeftMouseButtonPressed();

    /**
     * Returns whether the right mouse button is currently pressed. Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     *
     * @see #isLeftMouseButtonPressed()
     */
    boolean isRightMouseButtonPressed();

    /**
     * Returns whether the left mouse button was just clicked (released). Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     *
     * @see #wasRightMouseButtonClicked()
     */
    boolean wasLeftMouseButtonClicked();

    /**
     * Returns whether the right mouse button was just clicked (released). Use
     * {@link #getMouseX()} and {@link #getMouseY()} to get the current mouse
     * position.
     *
     * @see #wasLeftMouseButtonClicked()
     */
    boolean wasRightMouseButtonClicked();

    /**
     * Returns the x coordinate of the current mouse position within the GUI.
     *
     * @see #getMouseY()
     */
    double getMouseX();

    /**
     * Returns the y coordinate of the current mouse position within the GUI.
     *
     * @see #getMouseX()
     */
    double getMouseY();
}
