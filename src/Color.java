import static java.lang.Math.max;
import static java.lang.Math.min;

public class Color {
    
    public final int r, g, b;
    
    public Color(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }
    
    private static int clamp(int raw) {
        return max(0, min(255, raw));
    }
}
