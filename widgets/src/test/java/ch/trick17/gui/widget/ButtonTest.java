package ch.trick17.gui.widget;

import ch.trick17.gui.Gui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ButtonTest {

    @Mock
    Gui gui;

    @Test
    void hover() {
        var button = new Button("Test", 0, 0, 100, 50) {};
        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);

        button.onMouseEnter();
        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getHoverBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);

        button.onMouseExit();
        button.draw(gui);
        verify(gui, atLeastOnce()).setColor(button.getBackgroundColor());
        verify(gui, atLeastOnce()).fillRect(0, 0, 100, 50);
    }
}
