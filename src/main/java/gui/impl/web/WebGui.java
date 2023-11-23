package gui.impl.web;

import gui.Color;
import gui.Gui;
import gui.impl.GuiBase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.util.Locale.ROOT;

/**
 * Web-based implementation of {@link Gui}.
 */
public class WebGui extends GuiBase {

    private WebGuiSocket socket;
    private List<CharSequence> drawCommands;
    private final Set<String> loadedImages = new HashSet<>();

    public WebGui(String title, int width, int height) {
        super(title, width, height);
        drawCommands = applyCurrentSettings();
        WebGuiSocket.register(this);
    }

    private List<CharSequence> applyCurrentSettings() {
        return new ArrayList<>(List.of(
                "clear    ",
                format("setColor %d,%d,%d,%.3f", color.r, color.g, color.b, color.alpha / 255.0),
                format("setStkW  %.1f", strokeWidth),
                format("setRdStk %b", roundStroke),
                format("setFont  %d,%b", fontSize, bold),
                format("setTxtAl %s", textAlign.name().toLowerCase(ROOT)),
                format("setLnSpc %.3f", lineSpacing),
                format("setAlpha %.3f", alpha)));
    }

    void initialize(WebGuiSocket socket) {
        this.socket = socket;
    }

    @Override
    public void open() {
        super.open();
        var commands = new ArrayList<CharSequence>(List.of(
                "setTitle " + title,
                "setSize  " + width + "," + height));
        commands.addAll(drawCommands);
        socket.send(commands);
    }

    @Override
    public void close() {
        super.close();
        socket.close();
    }

    @Override
    protected void repaint(boolean clear) {
        socket.send(drawCommands);
        if (clear) {
            drawCommands = applyCurrentSettings();
        }
    }

    @Override
    public void setResizable(boolean resizable) {
        throw new UnsupportedOperationException();
    }

    void onEvent(String event) {
        var name = event.substring(0, 8);
        var args = event.substring(9);
        switch (name) {
            case "keyDown ":
                synchronized (inputLock) {
                    pressedInputs.add(new KeyInput(toKeyText(args)));
                }
                break;
            case "keyUp   ":
                synchronized (inputLock) {
                    var input = new KeyInput(toKeyText(args));
                    pressedInputs.remove(input);
                    releasedInputs.add(input);
                }
                break;
            case "mouseDwn":
                synchronized (inputLock) {
                    pressedInputs.add(new MouseInput(args.equals("0")));
                }
                break;
            case "mouseUp ":
                synchronized (inputLock) {
                    var input = new MouseInput(args.equals("0"));
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

    private String toKeyText(String keyCode) {
        return keyCode.toLowerCase(ROOT)
                .replace(" ", "space")
                .replace("arrow", "");
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        drawCommands.add(format("setColor %d,%d,%d,%.3f",
                color.r, color.g, color.b, color.alpha / 255.0));
    }

    @Override
    public void setStrokeWidth(double strokeWidth) {
        super.setStrokeWidth(strokeWidth);
        drawCommands.add(format("setStkW  %.1f", strokeWidth));
    }

    @Override
    public void setRoundStroke(boolean roundStroke) {
        super.setRoundStroke(roundStroke);
        drawCommands.add(format("setRdStk %b", roundStroke));
    }

    @Override
    public void setFontSize(int fontSize) {
        super.setFontSize(fontSize);
        drawCommands.add(format("setFont  %d,%b", fontSize, bold));
    }

    @Override
    public void setBold(boolean bold) {
        super.setBold(bold);
        drawCommands.add(format("setFont  %d,%b", fontSize, bold));
    }

    @Override
    public void setTextAlign(int textAlign) {
        super.setTextAlign(textAlign);
        drawCommands.add(format("setTxtAl %s", this.textAlign.name().toLowerCase(ROOT)));
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        super.setLineSpacing(lineSpacing);
        drawCommands.add(format("setLnSpc %.3f", lineSpacing));
    }

    @Override
    public void setAlpha(double alpha) {
        super.setAlpha(alpha);
        drawCommands.add(format("setAlpha %.3f", alpha));
    }

    @Override
    public double stringWidth(String string) {
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
        drawCommands.add(format("drawOval %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    @Override
    public void fillOval(double x, double y, double width, double height) {
        drawCommands.add(format("fillOval %.1f,%.1f,%.1f,%.1f", x, y, width, height));
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        drawCommands.add(format("drawLine %.1f,%.1f,%.1f,%.1f", x1, y1, x2, y2));
    }

    @Override
    public void drawPath(double[] coordinates) {
        if (coordinates.length >= 2) {
            var command = new StringBuilder("drawPath ");
            append(command, coordinates);
            drawCommands.add(command);
        }
    }

    @Override
    public void drawPolygon(double[] coordinates) {
        if (coordinates.length >= 2) {
            var command = new StringBuilder("drawPoly ");
            append(command, coordinates);
            drawCommands.add(command);
        }
    }

    @Override
    public void fillPolygon(double[] coordinates) {
        if (coordinates.length >= 2) {
            var command = new StringBuilder("fillPoly ");
            append(command, coordinates);
            drawCommands.add(command);
        }
    }

    @Override
    public void drawMultiPolygon(double[][] rings) {
        var command = new StringBuilder("drawMPly ");
        append(command, rings);
        drawCommands.add(command);
    }

    @Override
    public void fillMultiPolygon(double[][] rings) {
        var command = new StringBuilder("fillMPly ");
        append(command, rings);
        drawCommands.add(command);
    }

    private static void append(StringBuilder command, double[][] rings) {
        for (var ring : rings) {
            if (ring.length >= 2) {
                append(command, ring);
                command.append(" ");
            }
        }
        if (command.length() > 0) {
            command.setLength(command.length() - 1);
        }
    }

    private static void append(StringBuilder command, double[] coordinates) {
        var formatter = new Formatter(command, null);
        for (int i = 0; i < coordinates.length; i += 2) {
            formatter.format("%.1f,%.1f,", coordinates[i], coordinates[i + 1]);
        }
        command.setLength(command.length() - 1);
    }

    @Override
    public void drawString(String string, double x, double y) {
        // need escape newlines, as these are used as command separators
        var escaped = string.replace("\\", "\\\\").replace("\n", "\\n");
        drawCommands.add(format("drawStr  %.1f,%.1f,%s", x, y, escaped));
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
}
