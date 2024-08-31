package ch.trick17.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ColorTest {

    @Test
    public void parseHexCode3Digits() {
        var color = Color.parseHexCode("#000");
        assertEquals(0x00, color.r);
        assertEquals(0x00, color.g);
        assertEquals(0x00, color.b);
        assertEquals(0xFF, color.alpha);

        color = Color.parseHexCode("#3CE");
        assertEquals(0x33, color.r);
        assertEquals(0xCC, color.g);
        assertEquals(0xEE, color.b);
        assertEquals(0xFF, color.alpha);

        color = Color.parseHexCode("#a7b");
        assertEquals(0xAA, color.r);
        assertEquals(0x77, color.g);
        assertEquals(0xBB, color.b);
        assertEquals(0xFF, color.alpha);
    }

    @Test
    public void parseHexCode4Digits() {
        var color = Color.parseHexCode("#0000");
        assertEquals(0x00, color.r);
        assertEquals(0x00, color.g);
        assertEquals(0x00, color.b);
        assertEquals(0x00, color.alpha);

        color = Color.parseHexCode("#3CE1");
        assertEquals(0x33, color.r);
        assertEquals(0xCC, color.g);
        assertEquals(0xEE, color.b);
        assertEquals(0x11, color.alpha);

        color = Color.parseHexCode("#fbae");
        assertEquals(0xFF, color.r);
        assertEquals(0xBB, color.g);
        assertEquals(0xAA, color.b);
        assertEquals(0xEE, color.alpha);
    }

    @Test
    public void parseHexCode6Digits() {
        var color = Color.parseHexCode("#000000");
        assertEquals(0x00, color.r);
        assertEquals(0x00, color.g);
        assertEquals(0x00, color.b);
        assertEquals(0xFF, color.alpha);

        color = Color.parseHexCode("#123456");
        assertEquals(0x12, color.r);
        assertEquals(0x34, color.g);
        assertEquals(0x56, color.b);
        assertEquals(0xFF, color.alpha);

        color = Color.parseHexCode("#FFAB2E");
        assertEquals(0xFF, color.r);
        assertEquals(0xAB, color.g);
        assertEquals(0x2E, color.b);
        assertEquals(0xFF, color.alpha);

        color = Color.parseHexCode("#cf090E");
        assertEquals(0xCF, color.r);
        assertEquals(0x09, color.g);
        assertEquals(0x0E, color.b);
        assertEquals(0xFF, color.alpha);
    }

    @Test
    public void parseHexCode8Digits() {
        var color = Color.parseHexCode("#00000000");
        assertEquals(0x00, color.r);
        assertEquals(0x00, color.g);
        assertEquals(0x00, color.b);
        assertEquals(0x00, color.alpha);

        color = Color.parseHexCode("#12345678");
        assertEquals(0x12, color.r);
        assertEquals(0x34, color.g);
        assertEquals(0x56, color.b);
        assertEquals(0x78, color.alpha);

        color = Color.parseHexCode("#61ddf1ab");
        assertEquals(0x61, color.r);
        assertEquals(0xDD, color.g);
        assertEquals(0xF1, color.b);
        assertEquals(0xAB, color.alpha);

        color = Color.parseHexCode("#87EeBbAF");
        assertEquals(0x87, color.r);
        assertEquals(0xEE, color.g);
        assertEquals(0xBB, color.b);
        assertEquals(0xAF, color.alpha);
    }

    @Test
    public void parseHexCodeIllegal() {
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("000000");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("#FF");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("Hello");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("#FF00112");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("#FF001122A");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("#00CCGG");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Color.parseHexCode("#1B-");
        });
    }
}
