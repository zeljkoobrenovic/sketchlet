package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.programming.macros.MacroThread;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 10-11-12
 * Time: 20:23
 * To change this template use File | Settings | File Templates.
 */
public class MouseProcessor {
    public static final int MOUSE_LEFT_BUTTON_CLICK = 0;
    public static final int MOUSE_LEFT_BUTTON_PRESS = 1;
    public static final int MOUSE_LEFT_BUTTON_RELEASE = 2;
    public static final int MOUSE_RIGHT_BUTTON_CLICK = 3;
    public static final int MOUSE_RIGHT_BUTTON_PRESS = 4;
    public static final int MOUSE_RIGHT_BUTTON_RELEASE = 5;
    public static final int MOUSE_MIDDLE_BUTTON_CLICK = 6;
    public static final int MOUSE_MIDDLE_BUTTON_PRESS = 7;
    public static final int MOUSE_MIDDLE_BUTTON_RELEASE = 8;
    public static final int MOUSE_DOUBLE_CLICK = 9;
    public static final int MOUSE_ENTRY = 10;
    public static final int MOUSE_EXIT = 11;
    public static final int MOUSE_WHEEL_UP = 12;
    public static final int MOUSE_WHEEL_DOWN = 13;
    public static String[] MOUSE_EVENT_TYPES = {
            "Left Button Click",
            "Left Button Press",
            "Left Button Release",
            "Right Button Click",
            "Right Button Press",
            "Right Button Release",
            "Middle Button Click",
            "Middle Button Press",
            "Middle Button Release",
            "Double Click",
            "Mouse Entry",
            "Mouse Exit",
            "Mouse Wheel Up",
            "Mouse Wheel Down"
    };
    public List<MouseEventMacro> mouseEventMacros = new Vector<MouseEventMacro>();

    public MouseEventMacro getMouseEventMacro(String mouseEvent) {
        for (MouseEventMacro mouseEventMacro : this.mouseEventMacros) {
            if (mouseEventMacro.getEventName().equalsIgnoreCase(mouseEvent)) {
                return mouseEventMacro;
            }
        }
        return null;
    }

    public int getMouseActionsCount() {
        return mouseEventMacros.size();
    }

    public void dispose() {
        for (EventMacro eventMacro : mouseEventMacros) {
            eventMacro.dispose();
        }
        this.mouseEventMacros.clear();
    }

    public void processAction(MouseEvent e, JFrame frame, int row) {
        processAction(frame, row);
    }

    public void processAction(JFrame frame, int row) {
        EventMacro mouseEventMacro = this.getMouseEventMacro(MouseProcessor.MOUSE_EVENT_TYPES[row]);
        if (mouseEventMacro != null) {
            Page page = Workspace.getPage();
            MacroThread mt = new MacroThread(mouseEventMacro.getMacro(), "", "", "");
            page.activeMacros.add(mt);
        }
    }

    public void processAction(MouseEvent e, final JFrame frame, int[] rows) {
        processAction(e.getButton(), frame, rows);
    }

    public void processAction(int btn, final JFrame frame, int[] rows) {
        int r = 0;
        switch (btn) {
            case MouseEvent.BUTTON1:
                r = rows[0];
                break;
            case MouseEvent.BUTTON2:
                r = rows[1];
                break;
            case MouseEvent.BUTTON3:
                r = rows[2];
                break;
        }

        final int _r = r;
        final String strEvent = MouseProcessor.MOUSE_EVENT_TYPES[_r];

        EventMacro mouseEventMacro = this.getMouseEventMacro(strEvent);
        if (mouseEventMacro != null) {
            Page page = Workspace.getPage();
            MacroThread mt = new MacroThread(mouseEventMacro.getMacro(), "", "", "");
            page.activeMacros.add(mt);
        }
    }
}
