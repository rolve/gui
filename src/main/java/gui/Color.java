package gui;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * A class to represent colors.
 */
public final class Color {

    public final int r, g, b, alpha;

    /**
     * Creates a new color. The three parameters represent the red, green, and blue
     * channel and are expected to be in the 0&ndash;255 range. Values outside this
     * range will be clamped. Alpha defaults to 255 (no transparency).
     */
    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Creates a new color with a transparency value (alpha). The four parameters
     * represent the red, green, and blue channel, and an additional "alpha"
     * channel that represents the "opposite of transparency" (an alpha value of
     * 0 means fully transparent; a value of 255 means no transparency). All
     * values are expected to be in the 0&ndash;255 range. Values outside this
     * range will be clamped.
     */
    public Color(int r, int g, int b, int alpha) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.alpha = clamp(alpha);
    }

    /**
     * Creates a new color based on the given hex code, following the
     * <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/hex-color">format used on the web</a>,
     * e.g., #03FF99. The 3-, 4-, 6-, and 8-digit syntaxes are all supported.
     */
    public static Color parseHexCode(String hex) {
        if (hex.matches("#[0-9A-Fa-f]{3}|#[0-9A-Fa-f]{4}")) {
            var r = parseInt(hex.substring(1, 2), 16);
            var g = parseInt(hex.substring(2, 3), 16);
            var b = parseInt(hex.substring(3, 4), 16);
            var a = hex.length() == 5 ? parseInt(hex.substring(4, 5), 16) : 15;
            return new Color(17 * r, 17 * g, 17 * b, 17 * a);
        } else if (hex.matches("#[0-9A-Fa-f]{6}|#[0-9A-Fa-f]{8}")) {
            var r = parseInt(hex.substring(1, 3), 16);
            var g = parseInt(hex.substring(3, 5), 16);
            var b = parseInt(hex.substring(5, 7), 16);
            var a = hex.length() == 9 ? parseInt(hex.substring(7, 9), 16) : 255;
            return new Color(r, g, b, a);
        } else {
            throw new IllegalArgumentException("invalid hex code: " + hex);
        }
    }

    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }

    /**
     * Returns an integer representation of this color, excluding the alpha
     * channel.
     */
    public int toRgbInt() {
        return r << 16 | g << 8 | b;
    }

    /**
     * Returns an integer representation of this color, including the alpha
     * channel, which occupies the bits 24 to 31.
     */
    public int toRgbaInt() {
        return alpha << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public int hashCode() {
        var prime = 31;
        var result = 1;
        result = prime * result + r;
        result = prime * result + g;
        result = prime * result + b;
        result = prime * result + alpha;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (Color) obj;
        return r == other.r && g == other.g && b == other.b && alpha == other.alpha;
    }
}