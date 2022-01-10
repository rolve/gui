package gui;

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
        final int prime = 31;
        int result = 1;
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
        Color other = (Color) obj;
        return r == other.r && g == other.g && b == other.b && alpha == other.alpha;
    }
}