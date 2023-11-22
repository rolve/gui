package gui.impl.web;

import gui.Color;
import gui.Gui;
import gui.component.Component;
import gui.component.Drawable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

/**
 * Web-based implementation of {@link Gui}.
 */
public class WebGui implements Gui {

    private final String title;
    private final int width;
    private final int height;

    private WebGuiSocket socket;

    private List<String> drawCommands;

    private Color color = new Color(0, 0, 0);

    private final Set<String> loadedImages = new HashSet<>();

    private final Object inputLock = new Object();
    private final Set<Input> pressedInputs = new HashSet<>();
    private final Set<Input> releasedInputs = new HashSet<>();
    private final Set<Input> pressedSnapshot = new HashSet<>();
    private final Set<Input> releasedSnapshot = new HashSet<>();

    private volatile double mouseX = 0;
    private volatile double mouseY = 0;

    private volatile boolean open;

    private long lastRefreshTime = 0;

    private final List<Component> components = new ArrayList<>();

    public WebGui(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        drawCommands = applyCurrentSettings();

        WebGuiSocket.register(this);
    }

    private List<String> applyCurrentSettings() {
        return new ArrayList<>(List.of(
                "clear    ",
                "setColor " + color.r + "," + color.g + "," + color.b));
    }

    void initialize(WebGuiSocket socket) {
        this.socket = socket;
    }

    @Override
    public void open() {
        var commands = new ArrayList<>(List.of(
                "setTitle " + title,
                "setSize  " + width + "," + height));
        commands.addAll(drawCommands);
        socket.send(commands);
        lastRefreshTime = System.currentTimeMillis();
        open = true;
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
    public void refresh(int waitTime) {
        refresh(waitTime, false);
    }

    @Override
    public void refreshAndClear(int waitTime) {
        refresh(waitTime, true);
    }

    private void refresh(int waitTime, boolean clear) {
        runComponents();

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

        socket.send(drawCommands);
        if (clear) {
            drawCommands = applyCurrentSettings();
        }

        pressedSnapshot.clear();
        releasedSnapshot.clear();
        synchronized (inputLock) {
            pressedSnapshot.addAll(pressedInputs);
            releasedSnapshot.addAll(releasedInputs);
            releasedInputs.clear();
        }
    }

    private void runComponents() {
        for (var comp : components) {
            if (comp instanceof Drawable) {
                var d = (Drawable) comp;
                d.draw(this);
            }
        }
    }

    @Override
    public void setResizable(boolean resizable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    void onEvent(String event) {
        var name = event.substring(0, 8);
        var args = event.substring(9);
        switch (name) {
            case "keyDown ":
                synchronized (inputLock) {
                    pressedInputs.add(new KeyInput(args));
                }
                break;
            case "keyUp   ":
                synchronized (inputLock) {
                    pressedInputs.remove(new KeyInput(args));
                    releasedInputs.add(new KeyInput(args));
                }
                break;
            case "mouseDwn":
                synchronized (inputLock) {
                    pressedInputs.add(new MouseInput(args.equals("0")));
                }
                break;
            case "mouseUp ":
                var input = new MouseInput(args.equals("0"));
                synchronized (inputLock) {
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
                break;
            case "mouseMov":
                var parts = args.split(",");
                mouseX = parseDouble(parts[0]);
                mouseY = parseDouble(parts[1]);
                break;
            default:
                System.out.println("Unknown event: " + event);
        }
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

    @Override
    public void setColor(Color color) {
        this.color = color;
        drawCommands.add("setColor " + color.r + "," + color.g + "," + color.b);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setStrokeWidth(double strokeWidth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getStrokeWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRoundStroke(boolean roundStroke) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoundStroke() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFontSize(int fontSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFontSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBold(boolean bold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBold() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double stringWidth(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextAlignLeft() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextAlignCenter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextAlignRight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextAlign(int textAlign) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTextAlign() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getLineSpacing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlpha(double alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getAlpha() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawRect(double x, double y, double width, double height) {
        drawCommands.add(format("drawRect %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    @Override
    public void fillRect(double x, double y, double width, double height) {
        drawCommands.add(format("fillRect %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    @Override
    public void drawOval(double x, double y, double width, double height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillOval(double x, double y, double width, double height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawPath(double[] coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawPolygon(double[] coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillPolygon(double[] coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawMultiPolygon(double[][] rings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillMultiPolygon(double[][] rings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawString(String string, double x, double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawStringCentered(String string, double x, double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawImage(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        drawCommands.add(format("drawImg  %.1f,%.1f,%.1f,%.1f,%s", x, y, scale, angle, path));
    }

    @Override
    public void drawImageCentered(String path, double x, double y, double scale, double angle) {
        ensureLoaded(path);
        drawCommands.add(format("drawImgC %.1f,%.1f,%.1f,%.1f,%s", x, y, scale, angle, path));
    }

    @Override
    public List<String> getPressedKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getTypedKeys() {
        throw new UnsupportedOperationException();
    }

    private void ensureLoaded(String imagePath) {
        if (!loadedImages.contains(imagePath)) {
            try (var res = getClass().getClassLoader().getResourceAsStream(imagePath)) {
                byte[] bytes;
                if (res != null) {
                    bytes = res.readAllBytes();
                } else {
                    bytes = readAllBytes(Path.of(imagePath));
                }
                socket.sentImage(imagePath, bytes);
            } catch (IOException e) {
                throw new Error("could not load image \"" + imagePath + "\"", e);
            }
            loadedImages.add(imagePath);
        }
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
    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }

    @Override
    public boolean isRightMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(false));
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

    private static class Input {}

    private static class KeyInput extends Input {
        String key;

        KeyInput(String keyText) {
            this.key = keyText.toLowerCase()
                    .replace(" ", "space")
                    .replace("arrow", "");
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
}
