package ch.trick17.gui.widget;

import ch.trick17.gui.Gui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ButtonTest {

    @Mock
    Gui gui;

    @Test
    void hover() {
        var button = new Button("Test", 0, 0, 100, 50) {};
        assertFalse(button.isHovered());

        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);

        button.onMouseEnter();
        assertTrue(button.isHovered());

        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getHoveredBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);

        button.onMouseExit();
        assertFalse(button.isHovered());

        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);
    }
}
