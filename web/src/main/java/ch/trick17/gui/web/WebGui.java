package ch.trick17.gui.web;

import ch.trick17.gui.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;

public class WebGui {

    private final String title;
    private final int width;
    private final int height;

    private WebGuiSocket socket;

    private List<String> drawCommands;
    private List<String> drawSnapshot;

    private Color color = new Color(0, 0, 0);

    private final Object inputLock = new Object();
    private final Set<Input> pressedInputs = new HashSet<>();
    private final Set<Input> releasedInputs = new HashSet<>();
    private final Set<Input> pressedSnapshot = new HashSet<>();
    private final Set<Input> releasedSnapshot = new HashSet<>();

    private volatile double mouseX = 0;
    private volatile double mouseY = 0;

    private volatile boolean open = true;

    private long lastRefreshTime = 0;

    public WebGui(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        drawCommands = applyCurrentSettings();
        drawSnapshot = new ArrayList<>();

        WebGuiSocket.register(this);
    }

    void initialize(WebGuiSocket socket) {
        this.socket = socket;
        socket.send("setTitle " + title,
                    "setSize  " + width + "," + height);
    }

    public boolean isOpen() {
        return open;
    }

    void close() {
        open = false;
    }

    public void refreshAndClear(int waitTime) {
        refresh(waitTime, true);
    }

    private void refresh(int waitTime, boolean clear) {
        if (clear) {
            synchronized (this) {
                drawSnapshot = drawCommands;
                drawCommands = applyCurrentSettings();
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
            } catch (InterruptedException ignored) {
            }
        }
        lastRefreshTime = System.currentTimeMillis();

        repaint();

        pressedSnapshot.clear();
        releasedSnapshot.clear();
        synchronized (inputLock) {
            pressedSnapshot.addAll(pressedInputs);
            releasedSnapshot.addAll(releasedInputs);
            releasedInputs.clear();
        }
    }

    private List<String> applyCurrentSettings() {
        return new ArrayList<>(List.of(
                "clear    ",
                "setColor " + color.r + "," + color.g + "," + color.b));
    }

    private void repaint() {
        socket.send(drawSnapshot);
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

    public void setColor(int red, int green, int blue) {
        this.color = new Color(red, green, blue);
        drawCommands.add("setColor " + red + "," + green + "," + blue);
    }

    public void drawRect(double x, double y, double width, double height) {
        drawCommands.add(format("drawRect %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    public void fillRect(double x, double y, double width, double height) {
        drawCommands.add(format("fillRect %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    public boolean isKeyPressed(String keyName) {
        return pressedSnapshot.contains(new KeyInput(keyName));
    }

    public boolean wasKeyTyped(String keyName) {
        return releasedSnapshot.contains(new KeyInput(keyName));
    }

    public boolean isLeftMouseButtonPressed() {
        return pressedSnapshot.contains(new MouseInput(true));
    }

    public boolean wasLeftMouseButtonClicked() {
        return releasedSnapshot.contains(new MouseInput(true));
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    private static class Input {
    }

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
