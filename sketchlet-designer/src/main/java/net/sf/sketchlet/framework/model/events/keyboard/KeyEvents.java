package net.sf.sketchlet.framework.model.events.keyboard;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class KeyEvents {
    private static Vector<KeyboardEvent> events = new Vector<KeyboardEvent>();

    static {
        createEvents();
    }

    public static int getKeyCode(String codeName) {
        for (KeyboardEvent e : events) {
            if (e.getName().equals(codeName)) {
                return e.getKeyCode();
            }
        }

        return -1;
    }


    public static String getCodeName(int keyCode) {
        for (KeyboardEvent e : events) {
            if (e.getKeyCode() == keyCode) {
                return e.getName();
            }
        }

        return "";
    }

    public static JComboBox getComboBox() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("");

        for (KeyboardEvent e : events) {
            comboBox.addItem(e.getName());
        }

        return comboBox;
    }

    private static void createEvents() {
        events.add(new KeyboardEvent("Enter", KeyEvent.VK_ENTER));
        events.add(new KeyboardEvent("Backspace", KeyEvent.VK_BACK_SPACE));
        events.add(new KeyboardEvent("Tab", KeyEvent.VK_TAB));
        events.add(new KeyboardEvent("Cancel", KeyEvent.VK_CANCEL));
        events.add(new KeyboardEvent("Caps Lock", KeyEvent.VK_CAPS_LOCK));
        events.add(new KeyboardEvent("Esc", KeyEvent.VK_ESCAPE));
        events.add(new KeyboardEvent("Space", KeyEvent.VK_SPACE));
        events.add(new KeyboardEvent("Page Up", KeyEvent.VK_PAGE_UP));
        events.add(new KeyboardEvent("Page Down", KeyEvent.VK_PAGE_DOWN));
        events.add(new KeyboardEvent("End", KeyEvent.VK_END));
        events.add(new KeyboardEvent("Home", KeyEvent.VK_HOME));

        events.add(new KeyboardEvent("Left", KeyEvent.VK_LEFT));
        events.add(new KeyboardEvent("Up", KeyEvent.VK_UP));
        events.add(new KeyboardEvent("Right", KeyEvent.VK_RIGHT));
        events.add(new KeyboardEvent("Down", KeyEvent.VK_DOWN));

        events.add(new KeyboardEvent(",", KeyEvent.VK_COMMA));
        events.add(new KeyboardEvent("-", KeyEvent.VK_MINUS));
        events.add(new KeyboardEvent(".", KeyEvent.VK_PERIOD));
        events.add(new KeyboardEvent("/", KeyEvent.VK_SLASH));

        for (int i = 0; i <= 9; i++) {
            events.add(new KeyboardEvent("" + i, KeyEvent.VK_0 + i));
        }

        events.add(new KeyboardEvent(";", KeyEvent.VK_SEMICOLON));
        events.add(new KeyboardEvent("=", KeyEvent.VK_EQUALS));

        events.add(new KeyboardEvent("A", KeyEvent.VK_A));
        events.add(new KeyboardEvent("B", KeyEvent.VK_B));
        events.add(new KeyboardEvent("C", KeyEvent.VK_C));
        events.add(new KeyboardEvent("D", KeyEvent.VK_D));
        events.add(new KeyboardEvent("E", KeyEvent.VK_E));
        events.add(new KeyboardEvent("F", KeyEvent.VK_F));
        events.add(new KeyboardEvent("G", KeyEvent.VK_G));
        events.add(new KeyboardEvent("H", KeyEvent.VK_H));
        events.add(new KeyboardEvent("I", KeyEvent.VK_I));
        events.add(new KeyboardEvent("J", KeyEvent.VK_J));
        events.add(new KeyboardEvent("K", KeyEvent.VK_K));
        events.add(new KeyboardEvent("L", KeyEvent.VK_L));
        events.add(new KeyboardEvent("M", KeyEvent.VK_M));
        events.add(new KeyboardEvent("N", KeyEvent.VK_N));
        events.add(new KeyboardEvent("O", KeyEvent.VK_O));
        events.add(new KeyboardEvent("P", KeyEvent.VK_P));
        events.add(new KeyboardEvent("Q", KeyEvent.VK_Q));
        events.add(new KeyboardEvent("R", KeyEvent.VK_R));
        events.add(new KeyboardEvent("S", KeyEvent.VK_S));
        events.add(new KeyboardEvent("T", KeyEvent.VK_T));
        events.add(new KeyboardEvent("U", KeyEvent.VK_U));
        events.add(new KeyboardEvent("V", KeyEvent.VK_V));
        events.add(new KeyboardEvent("W", KeyEvent.VK_W));
        events.add(new KeyboardEvent("X", KeyEvent.VK_X));
        events.add(new KeyboardEvent("Y", KeyEvent.VK_Y));
        events.add(new KeyboardEvent("Z", KeyEvent.VK_Z));
        events.add(new KeyboardEvent("[", KeyEvent.VK_OPEN_BRACKET));
        events.add(new KeyboardEvent("\\", KeyEvent.VK_BACK_SLASH));
        events.add(new KeyboardEvent("]", KeyEvent.VK_CLOSE_BRACKET));

        events.add(new KeyboardEvent("Del", KeyEvent.VK_DELETE));
        events.add(new KeyboardEvent("Num Lock", KeyEvent.VK_NUM_LOCK));
        events.add(new KeyboardEvent("Scroll Lock", KeyEvent.VK_SCROLL_LOCK));


        for (int i = 1; i <= 12; i++) {
            events.add(new KeyboardEvent("F" + i, KeyEvent.VK_F1 + i - 1));
        }
    }
}
