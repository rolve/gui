package ch.trick17.gui.impl;

import ch.trick17.gui.Color;
import ch.trick17.gui.Gui;
import ch.trick17.gui.component.EventListener;
import ch.trick17.gui.component.*;

import java.awt.event.KeyEvent;
import java.util.*;

import static java.lang.Double.isFinite;
import static java.lang.Integer.signum;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.newSetFromMap;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;

public abstract class GuiBase implements Gui {

    public static final char CHAR_UNDEFINED = KeyEvent.CHAR_UNDEFINED;

    protected final String title;
    protected volatile double width;
    protected volatile double height;

    protected long lastRefreshTime; // nanoseconds
    private volatile boolean open;

    protected Color color = new Color(0, 0, 0);
    protected double strokeWidth = 1;
    protected boolean roundStroke = false;
    protected String fontFamily = "sansserif";
    protected int fontSize = 11;
    protected boolean bold = false;
    protected boolean italic = false;
    protected TextAlign textAlign = TextAlign.LEFT;
    protected double lineSpacing = 1.0;
    protected double alpha = 1;

    protected volatile double mouseX = 0;
    protected volatile double mouseY = 0;

    protected final Object inputLock = new Object();
    protected final Set<Input> pressedInputs = new HashSet<>();
    protected final Set<Input> releasedInputs = new HashSet<>();
    protected final Set<Input> pressedSnapshot = new HashSet<>();
    protected final Set<Input> releasedSnapshot = new HashSet<>();

    private final Set<Hoverable> hovered = newSetFromMap(new IdentityHashMap<>());
    private final List<Component> components = new ArrayList<>();

    public GuiBase(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    protected void runComponents() {
        if (components.isEmpty()) {
            return;
        }

        // components are run with default settings, so save current settings
        var prevColor = color;
        var prevStrokeWidth = strokeWidth;
        var prevRoundStroke = roundStroke;
        var prevFontFamily = fontFamily;
        var prevFontSize = fontSize;
        var prevBold = bold;
        var prevItalic = italic;
        var prevTextAlign = textAlign;
        var prevLineSpacing = lineSpacing;
        var prevAlpha = alpha;

        var mx = mouseX;
        var my = mouseY;
        var leftClicked = wasLeftMouseButtonClicked();
        var rightClicked = wasRightMouseButtonClicked();
        var componentsSnapshot = List.copyOf(components);
        for (var comp : componentsSnapshot) {
            if (comp instanceof Hoverable) {
                var h = (Hoverable) comp;
                if (h.getInteractiveArea(this).contains(mx, my) && hovered.add(h)) {
                    h.onMouseEnter();
                } else if (!h.getInteractiveArea(this).contains(mx, my) && hovered.remove(h)) {
                    h.onMouseExit();
                }
            }
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
            if (comp instanceof EventListener) {
                var e = (EventListener) comp;
                for (var input : pressedSnapshot) {
                    if (input instanceof KeyInput) {
                        e.onKeyPress(((KeyInput) input).keyName, ((KeyInput) input).keyChar);
                    } else if (input instanceof MouseInput) {
                        e.onMouseButtonPress(mx, my, ((MouseInput) input).left);
                    }
                }
                for (var input : releasedSnapshot) {
                    if (input instanceof KeyInput) {
                        e.onKeyRelease(((KeyInput) input).keyName, ((KeyInput) input).keyChar);
                    } else if (input instanceof MouseInput) {
                        e.onMouseButtonRelease(mx, my, ((MouseInput) input).left);
                    }
                }
            }
            if (comp instanceof Drawable) {
                var d = (Drawable) comp;
                resetSettings();
                d.draw(this);
            }
        }

        // restore settings
        setColor(prevColor);
        setStrokeWidth(prevStrokeWidth);
        setRoundStroke(prevRoundStroke);
        setFontFamily(prevFontFamily);
        setFontSize(prevFontSize);
        setBold(prevBold);
        setItalic(prevItalic);
        setTextAlign(prevTextAlign.toInt());
        setLineSpacing(prevLineSpacing);
        setAlpha(prevAlpha);
    }

    @Override
    public void open() {
        open = true;
        lastRefreshTime = System.nanoTime();
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
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
    public void refresh(int waitTime) {
        refresh(waitTime, false);
    }

    @Override
    public void refreshAndClear(int waitTime) {
        refresh(waitTime, true);
    }

    private void refresh(int waitTime, boolean clear) {
        var targetTime = lastRefreshTime + waitTime * 1_000_000L; // nanos
        while (System.nanoTime() < targetTime) {
            var diff = (targetTime - System.nanoTime()) / 1_000_000; // millis
            if (diff >= 2) { // account for Thread.sleep() inaccuracy
                try {
                    Thread.sleep(diff - 2);
                } catch (InterruptedException ignored) {}
            } else {
                Thread.onSpinWait();
            }
        }
        lastRefreshTime = System.nanoTime();

        pressedSnapshot.clear();
        releasedSnapshot.clear();
        synchronized (inputLock) {
            pressedSnapshot.addAll(pressedInputs);
            releasedSnapshot.addAll(releasedInputs);
            releasedInputs.clear();
        }

        runComponents();
        repaint(clear);
    }

    protected abstract void repaint(boolean clear);

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
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public double getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public void setRoundStroke(boolean roundStroke) {
        this.roundStroke = roundStroke;
    }

    @Override
    public boolean isRoundStroke() {
        return roundStroke;
    }

    @Override
    public void setFontFamily(String fontFamily) throws UnsupportedOperationException {
        this.fontFamily = fontFamily;
    }

    @Override
    public String getFontFamily() {
        return fontFamily;
    }

    @Override
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setBold(boolean bold) {
        this.bold = bold;
    }

    @Override
    public boolean isBold() {
        return bold;
    }

    @Override
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    @Override
    public boolean isItalic() {
        return italic;
    }

    @Override
    public void setTextAlign(int textAlign) {
        this.textAlign = TextAlign.fromInt(textAlign);
    }

    @Override
    public int getTextAlign() {
        return textAlign.toInt();
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        this.lineSpacing = clampPositive(lineSpacing);
    }

    protected final double clampPositive(double d) {
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
        this.alpha = max(0, min(1, alpha));
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    /*
     * Input
     */

    @Override
    public List<String> getPressedKeys() {
        return pressedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).keyName)
                .collect(toList());
    }

    @Override
    public List<String> getTypedKeys() {
        return releasedSnapshot.stream()
                .filter(i -> i instanceof KeyInput)
                .map(i -> ((KeyInput) i).keyName)
                .collect(toList());
    }

    @Override
    public boolean isKeyPressed(String keyName) {
        return pressedSnapshot.stream()
                .anyMatch(i -> i instanceof KeyInput &&
                               ((KeyInput) i).keyName.equalsIgnoreCase(keyName));
    }

    @Override
    public boolean wasKeyTyped(String keyName) {
        return releasedSnapshot.stream()
                .anyMatch(i -> i instanceof KeyInput &&
                               ((KeyInput) i).keyName.equalsIgnoreCase(keyName));
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

    protected enum TextAlign {
        LEFT, CENTER, RIGHT;

        public static TextAlign fromInt(int textAlign) {
            return values()[signum(textAlign) + 1];
        }

        public int toInt() {
            return ordinal() - 1;
        }
    }

    protected static class Input {}

    protected static final class KeyInput extends Input {
        private final String keyName;
        private final char keyChar;

        public KeyInput(String keyName, char keyChar) {
            this.keyName = keyName.toLowerCase(ROOT);
            this.keyChar = keyChar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof KeyInput)) {
                return false;
            }
            var keyInput = (KeyInput) o;
            return keyChar == keyInput.keyChar && keyName.equals(keyInput.keyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyName, keyChar);
        }
    }

    protected static final class MouseInput extends Input {
        private final boolean left;

        public MouseInput(boolean left) {
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
}
