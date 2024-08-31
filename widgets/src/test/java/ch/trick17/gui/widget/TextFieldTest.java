package ch.trick17.gui.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextFieldTest {

    @Test
    void focus() {
        var field = new TextField(50, 50, 100, 50);
        assertFalse(field.isFocussed());

        field.onMouseButtonRelease(100, 75, true);
        assertTrue(field.isFocussed());

        field.onMouseButtonRelease(200, 75, true);
        assertFalse(field.isFocussed());

        field.onMouseButtonRelease(0, 0, true);
        assertFalse(field.isFocussed());

        field.onMouseButtonRelease(50, 50, true);
        assertTrue(field.isFocussed());

        field.onMouseButtonRelease(100, 75, false); // right click
        assertTrue(field.isFocussed());
    }

    @Test
    void input() {
        var field = new TextField(0, 0, 100, 50);
        field.onMouseButtonRelease(0, 0, true);
        assertEquals("", field.getText());

        field.onKeyRelease("a", 'a');
        assertEquals("a", field.getText());

        field.onKeyRelease("space", ' ');
        assertEquals("a ", field.getText());

        field.onKeyRelease("b", 'B');
        assertEquals("a B", field.getText());

        field.onKeyRelease("back_space", '\b');
        assertEquals("a ", field.getText());
    }

    String capturedText = null;

    @Test
    void onTextChange() {
        var field = new TextField(0, 0, 100, 50) {
            protected void onTextChange(String text) {
                capturedText = text;
            }
        };
        field.onMouseButtonRelease(0, 0, true);
        field.onKeyRelease("a", 'a');
        assertEquals("a", capturedText);

        field.onKeyRelease("2", '@');
        assertEquals("a@", capturedText);
    }
}
